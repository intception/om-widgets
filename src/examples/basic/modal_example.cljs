(ns examples.basic.modal-example
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [om-widgets.layouts :as layout :include-macros true]
            [dommy.core :as dommy :refer-macros [sel sel1]]
            [sablono.core :as html :refer-macros [html]]
            [om-widgets.modal-box :as mb]
            [om-widgets.core :as w]))


(defn modal-example
  [cursor owner]
  (reify
    om/IInitState
    (init-state [_]
      {:show-modal false})

    om/IRenderState
    (render-state [_ state]
      (html
        [:div.panel.panel-default
         [:div.panel-heading "Modal"]
         [:div.panel-body

          [:div.well
           (when (:show-small-modal state)
             (mb/modal-box cursor {:title (fn [_ _]
                                            (html
                                              [:h4 "Title"]))
                                   :close-fn #(om/set-state! owner :show-small-modal false)
                                   :close-on-esc true
                                   :body (fn [_ _]
                                           (html
                                             [:label "This is the modal's body..."]))
                                   :footer (fn [close-fn _]
                                             (html
                                               [:div
                                                (w/button "Close" {:onClick #(close-fn)
                                                                   :class-name "btn btn-link"})
                                                (w/button (html [:div
                                                                 [:span {:class "icn-ok"}]
                                                                 "Save Changes"])
                                                          {:onClick #(close-fn)
                                                           :class-name "btn btn-primary"})]))
                                   :size :sm}))

           [:button.btn.btn-default {:on-click #(om/set-state! owner :show-small-modal true)}
            "Open Small Modal"]]

          [:div.well
           (when (:show-modal state)
             (mb/modal-box cursor {:title (fn [_ _]
                                            (html
                                              [:h4 "Title"]))
                                   :close-fn #(om/set-state! owner :show-modal false)
                                   :close-on-esc true
                                   :body (fn [_ _]
                                           (html
                                             [:label "This is the modal's body..."]))
                                   :footer (fn [close-fn _]
                                             (html
                                               [:div
                                                (w/button "Close" {:onClick #(close-fn)
                                                                   :class-name "btn btn-link"})
                                                (w/button (html [:div
                                                                 [:span {:class "icn-ok"}]
                                                                 "Save Changes"])
                                                          {:onClick #(close-fn)
                                                           :class-name "btn btn-primary"})]))}))

           [:button.btn.btn-default {:on-click #(om/set-state! owner :show-modal true)}
            "Open Medium Modal"]]


          [:div.well
           (when (:show-large-modal state)
             (mb/modal-box cursor {:title (fn [_ _]
                                            (html
                                              [:h4 "Title"]))
                                   :close-fn #(om/set-state! owner :show-large-modal false)
                                   :close-on-esc true
                                   :body (fn [_ _]
                                           (html
                                             [:label "This is the modal's body..."]))
                                   :footer (fn [close-fn _]
                                             (html
                                               [:div
                                                (w/button "Close" {:onClick #(close-fn)
                                                                   :class-name "btn btn-link"})
                                                (w/button (html [:div
                                                                 [:span {:class "icn-ok"}]
                                                                 "Save Changes"])
                                                          {:onClick #(close-fn)
                                                           :class-name "btn btn-primary"})]))
                                   :size :lg}))

           [:button.btn.btn-default {:on-click #(om/set-state! owner :show-large-modal true)}
            "Open Large Modal"]]]]))))
