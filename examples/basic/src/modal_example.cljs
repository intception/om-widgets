(ns examples.basic.modal-example
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [om-widgets.layouts :as layout :include-macros true]
            [dommy.core :as dommy :refer-macros [sel sel1]]
            [sablono.core :as html :refer-macros [html]]
            [om-widgets.modal-box :as mb]
            [om-widgets.core :as w]))


(def ESC 27)

(defn key-event->keycode [e] (.-keyCode e))
(def legal-key #{ESC})

(defn handle-keydown [e owner]
  (let [k (key-event->keycode e)]
    (when (legal-key k)
      (condp = k
        ESC (om/set-state! owner :show-modal false)))))

;; TODO parent is a workarround, normally you should pass a core.async channel
(defn- modal-component [cursor parent]
  (mb/modal-box cursor {:title (fn [_ target]
                                 (html
                                   [:h4 "Title"]))
                        :close-fn #(om/set-state! parent :show-modal false)
                        :body (fn [_ target]
                                (html
                                  [:label "This is the modal's body..."]))
                        :footer (fn [close-fn target]
                                  (html
                                    [:div
                                     (w/button "Close" {:onClick #(close-fn)
                                                        :class-name "btn btn-link"})
                                     (w/button (html [:div
                                                      [:span {:class "icn-ok"}]
                                                      "Save Changes"])
                                               {:onClick #(close-fn)
                                                :class-name "btn btn-primary"})]))
                        :class-name "modal-lg"}))

(defn modal-sample
  [cursor owner opts]
  (reify
    om/IWillMount
    (will-mount [_] (dommy/listen! (sel1 :body) :keydown #(handle-keydown % owner)))

    om/IWillUnmount
    (will-unmount [_]
      (dommy/unlisten! (sel1 :body) :keydown #(handle-keydown % owner)))

    om/IDisplayName
    (display-name[_] "ModalSample")

    om/IInitState
    (init-state [_]
                {:show-modal false})

    om/IRenderState
    (render-state [this state]
                  (html
                    [:div.panel.panel-collapsable
                     [:div.panel-heading "Modal"
                      [:div.well
                       (when (:show-modal state)
                         (modal-component cursor owner))
                       [:button.btn.btn-default {:on-click #(om/set-state! owner :show-modal true)}
                        "Open Modal"]]]]))))
