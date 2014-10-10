(ns intception-widgets.modal-box
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [intception-widgets.forms :as f :include-macros true]
            [dommy.core :as dommy]
            [cljs.core.async :refer [put! chan <! alts! timeout]])
  (:use-macros [dommy.macros :only [sel1]]))

(enable-console-print!)
(defn- create-modal-box [_ owner]
  (reify

    om/IDidMount
    (did-mount [this]
      (-> (sel1 "body")
          (dommy/add-class! "modal-is-open")))

    om/IWillUnmount
    (will-unmount [this]
      (-> (sel1 "body")
          (dommy/remove-class! "modal-is-open")))

    om/IRenderState
    (render-state [this {:keys [title body footer] :as state}]
      (dom/div nil
        (dom/div #js {:className "overlay"})
        (dom/div #js {:className "modal-box"}
          (dom/div #js {:className "modal-dialog"}
            (dom/div #js {:className "modal-content"}
                     (if (string? title)
                       (dom/div #js {:className "modal-header"}
                                (dom/h4 #js {:className "modal-title"} title))
                       (when (vector? title)
                         (apply dom/div #js {:className "modal-header"} title)))

                     (if (string? body)
                       (dom/div #js {:className "modal-body"}
                                (dom/p nil body))
                       (when (and (vector? body) (not-empty body))
                         (apply dom/div #js {:className "modal-body"} body)))

                     (if (seq? footer)
                       (apply dom/div {:className "modal-footer"} footer)
                       (dom/div #js {:className "modal-footer"}
                                (dom/p nil footer))))))))))


(defn modal-box
  "Arguments title,body and footer  [string or vector of components]"
  [{:keys [title body footer]
    :or {body "Missing body parameter!"}}]
  (om/build create-modal-box {} {:state {:body body
                                         :footer footer
                                         :title title}}))

(defn install-modal-box!
  [owner]
  (when-let [config (om/get-state owner :mb_config)]
    (modal-box config)))

(defn alert
  [owner title message]
  (let [c (chan)]
    (go
     (om/set-state! owner
                    :mb_config
                    {:title title
                     :body message
                     :footer (f/with-owner owner
                               (f/button :btn-mb-alert-ok
                                         {:location :left
                                          :style :mb-alert-ok
                                          :icon  :icon-ok
                                          :text " OK"
                                          :on-click #(put! c :ok)}))})
     (when (<! c)
       (om/set-state! owner :mb_config nil)))))


(defn ok-cancel
  [owner title message]
  (let [c (chan)]
    (go
     (om/set-state! owner
                    :mb_config
                    {:title title
                     :body message
                     :footer (dom/div nil
                                      (f/with-owner owner
                                        (f/button :btn-mb-alert-ok
                                                  {:location :left
                                                   :icon :ok
                                                   :text " OK"
                                                   :on-click #(put! c :ok)}))
                                      (f/with-owner owner
                                        (f/button :btn-mb-alert-cancel
                                                  {:location :right
                                                   :style :danger
                                                   :icon :remove
                                                   :text " CANCEL"
                                                   :on-click #(put! c :cancel)})))
                     })
     (when-let [result (<! c)]
       (om/set-state! owner :mb_config nil)
       result))))
