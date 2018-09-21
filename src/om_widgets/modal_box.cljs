(ns om-widgets.modal-box
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [om-widgets.forms :as f :include-macros true]
            [dommy.core :as dommy :refer-macros [sel sel1]]
            [cljs.core.async :refer [put! chan <! alts! timeout]]))


(defn handle-keydown
  [owner event]
  (let [ESC 27
        k (.-keyCode event)]
    (when (#{ESC} k)
      (condp = k
        ESC ((om/get-state owner :close-fn))))))

(defn create-modal-box
  [target owner]
  (reify

    om/IDidMount
    (did-mount [this]
      (when (om/get-state owner :set-modal-is-open)
        (-> (sel1 "body")
          (dommy/add-class! "om-widgets-modal-is-open")))

      (when (om/get-state owner :close-on-esc)
        (let [handle-keydown-fn (partial handle-keydown owner)]
          (om/set-state! owner :handle-keydown-fn handle-keydown-fn)
          (dommy/listen! (sel1 :body) :keydown handle-keydown-fn))))

    om/IWillUnmount
    (will-unmount [this]
      (when (om/get-state owner :set-modal-is-open)
        (-> (sel1 "body")
          (dommy/remove-class! "om-widgets-modal-is-open")))

      (when-let [handle-keydown-fn (om/get-state owner :handle-keydown-fn)]
        (dommy/unlisten! (sel1 :body) :keydown handle-keydown-fn)))

    om/IRenderState
    (render-state [this {:keys [title body footer close-fn class-name size] :as state}]
      (dom/div #js {:className "modal"
                    :role "dialog"
                    :tabIndex -1
                    :style #js {:display "block"}}
               (dom/div #js {:className "om-widgets-overlay"})
               (dom/div #js {:className "om-widgets-modal-box"}
                        (dom/div #js {:className (str "om-widgets-modal-dialog "
                                                      class-name
                                                      (when size (str " modal-" (name size))))}
                                 (dom/div #js {:className "om-widgets-modal-content"}
                                          (if (string? title)
                                            (dom/div #js {:className "om-widgets-modal-header"}
                                                     (dom/h4 #js {:className "om-widgets-modal-title"} title))
                                            (when-let [title-seq (cond
                                                                   (fn? title) [(title close-fn target)]
                                                                   (seq? title) title
                                                                   (not= nil title) [title]
                                                                   :else nil)]
                                              (apply dom/div #js {:className "om-widgets-modal-header"} title-seq)))

                                          (when-let [body-seq (cond
                                                                (fn? body) [(body close-fn target)]
                                                                (seq? body) body
                                                                (not= nil body) [body]
                                                                :else nil)]
                                            (apply dom/div #js {:className "om-widgets-modal-body"} body-seq))

                                          (when-let [footer-seq (cond
                                                                  (fn? footer) [(footer close-fn target)]
                                                                  (seq? footer) footer
                                                                  (not= nil footer) [footer]
                                                                  :else nil)]
                                            (apply dom/div #js {:className "om-widgets-modal-footer"} footer-seq)))))))))


(defn modal-box
  "Arguments title, body and footer  [string or vector of components]"
  [target {:keys [title body footer close-fn class-name close-on-esc size set-modal-is-open]
           :or {body "Missing body parameter!"
                set-modal-is-open true}}]
  (om/build create-modal-box target {:state {:body body
                                             :close-fn close-fn
                                             :footer footer
                                             :title title
                                             :close-on-esc close-on-esc
                                             :size size
                                             :set-modal-is-open set-modal-is-open
                                             :class-name class-name}}))

(defn install-modal-box!
  [target owner]
  (when-let [config (om/get-state owner :mb_config)]
    (modal-box target config)))

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
                                                       :icon :icon-ok
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
                                                                :on-click #(put! c :cancel)})))})
      (when-let [result (<! c)]
        (om/set-state! owner :mb_config nil)
        result))))

(defn modal-launcher-component
  [_ owner opts]
  (reify
    om/IRenderState
    (render-state [this {:keys [html-fn title-fn body-fn footer-fn visible channel] :as state}]
      (dom/div nil
               (when visible
                 (om/build create-modal-box nil {:state {:body body-fn
                                                         :close-fn (fn [res]
                                                                     (om/set-state! owner :visible false)
                                                                     (put! channel (or res false)))
                                                         :footer footer-fn
                                                         :title title-fn}}))
               (html-fn (fn []
                          (go (let [channel (chan)]
                                (om/update-state! owner (fn [st]
                                                          (merge st {:visible true
                                                                     :channel channel})))
                                (<! channel)))))))))
(defn modal-launcher
  [html title body footer]
  (om/build modal-launcher-component nil {:state {:html-fn html
                                                  :title-fn title
                                                  :body-fn body
                                                  :footer-fn footer}}))

(defn do-modal
  [owner title body footer {:keys [class-name]}]
  (let [c (chan)]
    (go
      (let [close-fn (fn [result]
                       (put! c (or result false)))]
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
