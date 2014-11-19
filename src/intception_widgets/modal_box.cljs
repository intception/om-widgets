(ns intception-widgets.modal-box
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [intception-widgets.forms :as f :include-macros true]
            [dommy.core :as dommy]
            [cljs.core.async :refer [put! chan <! alts! timeout]])
  (:use-macros [dommy.macros :only [sel1]]))

(enable-console-print!)
(defn create-modal-box [_ owner]
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
    (render-state [this {:keys [title body footer close-fn class-name] :as state}]
      (dom/div #js {:className "modal"
                    :role "dialog"
                    :tabIndex -1
                    :style #js {:display "block"}}
               (dom/div #js {:className "overlay"})
               (dom/div #js {:className "modal-box"}
                        (dom/div #js {:className (str "modal-dialog " class-name)}
                                 (dom/div #js {:className "modal-content"}
                                          (if (string? title)
                                            (dom/div #js {:className "modal-header"}
                                                     (dom/h4 #js {:className "modal-title"} title))
                                            (when-let [title-seq (cond
                                                                  (fn? title) [(title close-fn)]
                                                                  (seq? title) title
                                                                  (not= nil title) [title]
                                                                  :else nil)]
                                              (apply dom/div #js {:className "modal-header"} title-seq)))

                                          (when-let [body-seq (cond
                                                               (fn? body) [(body close-fn)]
                                                               (seq? body) body
                                                               (not= nil body) [body]
                                                               :else nil)]
                                            (apply dom/div #js {:className "modal-body"} body-seq))

                                          (when-let [footer-seq (cond
                                                                 (fn? footer) [(footer close-fn)]
                                                                 (seq? footer) footer
                                                                 (not= nil footer) [footer]
                                                                 :else nil)]
                                            (apply dom/div #js {:className "modal-footer"} footer-seq)))))))))


(defn modal-box
  "Arguments title,body and footer  [string or vector of components]"
  [{:keys [title body footer close-fn class-name]
    :or {body "Missing body parameter!"}}]
  (om/build create-modal-box {} {:state {:body body
                                         :close-fn close-fn
                                         :footer footer
                                         :title title
                                         :class-name class-name}}))

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


(defn do-modal
  [owner title body footer {:keys [class-name]}]
  (let [c (chan)]
    (go
     (let [close-fn (fn [result]
                      (put! c result))]
       (om/set-state! owner
                      :mb_config
                      {:close-fn close-fn
                       :title title
                       :body body
                       :footer footer
                       :class-name class-name}))
     (let [result (<! c)]
       (om/set-state! owner :mb_config nil)
       result))))
