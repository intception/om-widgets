(ns om-widgets.dropdown
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [schema.core :as s :include-macros true]
            [om.core :as om :include-macros true]
            [shodan.console :as console :include-macros true]
            [cljs.core.async :refer [put! chan <! alts! timeout close!]]
            [sablono.core :as html :refer-macros [html]]))


;; TODO support headings: <li class="dropdown-header">Nav header</li>

;; ---------------------------------------------------------------------
;; Dropdown entry and divider

(defmulti build-entry (fn [entry app] (:type entry)))

(defmethod build-entry :entry
  [entry app]
  (reify
    om/IRenderState
    (render-state [this {:keys [channel] :as state}]
      (html
        ;; we use OnMouseDown because onBlur is triggered before
        ;; onClick event, we use onBlur to close the dropdown
        [:li (->> {:class (when (:disabled entry) "disabled")}
                  (merge (when-not (:disabled entry)
                           {:onMouseDown #(let [e (if (om/cursor? entry) @entry entry)]
                                            (put! channel {:type :entry-click
                                                           :value (:id e)
                                                           :link (:url e)})
                                            (.preventDefault %)
                                            (.stopPropagation %))})))
         [:a (->> {}
                  (#(if (:url entry)
                      (merge {:href (:url entry)} %)
                      %)))
          (:text entry)]]))))

(defmethod build-entry :divider
  [entry app]
  (reify
    om/IRenderState
    (render-state [this state]
      (html
       [:li {:class "divider"}]))))

;; ---------------------------------------------------------------------
;; Dropdown containers

(defn- build-dropdown-js-options
  [{:keys [size opened channel] :as state}]
  {:class ["om-widgets-dropdown dropdown btn-group"
           (str "btn-group-" (name (or size :md)))
           (when opened " open")]
   ;; tab index is set to 0 to force focus on the container,
   ;; this way, the onBlur event will be called when the user
   ;; clicks outside and we can close the dropdown.
   :tabIndex 0
   :onClick (fn [e]
              (put! (:channel state) {:type :open-dropdown})
              (.preventDefault e)
              (.stopPropagation e))
   :onBlur #(put! (:channel state) {:type :close-dropdown})})

(defn- channel-processing
  [cursor owner]
  (let [channel (om/get-state owner :channel)
        on-selection (om/get-state owner :on-selection)
        korks (om/get-state owner :korks)]
    (go-loop []
      (let [msg (<! channel)]
        (condp = (:type msg)
          :open-dropdown (om/set-state! owner :opened (not (om/get-state owner :opened)))
          :close-dropdown (om/set-state! owner :opened false)
          :entry-click (do
                         (when (:link msg)
                           (set! (.-location js/window) (:link msg)))

                         (when korks
                           (om/update! cursor korks (:value msg)))

                         (when on-selection
                           (on-selection (:value msg)))))
        (recur)))))

(defn- dropdown-menu
  [cursor owner]
  (reify
    om/IRenderState
    (render-state [this {:keys [channel items] :as state}]
      (html
       (vec (concat [:ul {:class "dropdown-menu"}]
                    (map #(om/build build-entry % {:state {:channel channel}}) items)))))))

(defn- dropdown-menu-container [cursor owner]
  (reify
    om/IInitState
    (init-state [_]
      {:opened false
       :channel (chan)})

    om/IWillMount
    (will-mount [_] (channel-processing cursor owner))

    om/IRenderState
    (render-state [_ {:keys [title items channel] :as state}]
      (html
       [:li (build-dropdown-js-options state)
        [:a {:class "dropdown-toggle" :title title}
         title
         [:span {:class "caret"}]]
        (om/build dropdown-menu cursor {:state {:channel channel
                                                :items items}})]))))

(defn- dropdown-container [cursor owner]
  (reify
    om/IInitState
    (init-state [_]
      {:opened false
       :channel (chan)})

    om/IWillMount
    (will-mount [_] (channel-processing cursor owner))

    om/IRenderState
    (render-state [_ {:keys [title as-link? icon items channel] :as state}]
      (let [title (str (when icon "  ") title " ")]
        (html
          [:div (build-dropdown-js-options state)
           (if as-link?
             [:a.btn-link
              (when icon [:span {:class icon}])
              title]

             [:button {:class "btn btn-default dropdown-toggle"
                       :type "button"}
              (when icon [:span {:class icon}])
              title
              [:span {:class "caret"}]])
           (om/build dropdown-menu cursor {:state {:channel channel
                                                   :items items}})])))))

;; ---------------------------------------------------------------------
;; Schema

(def EntrySchema
  "Schema for a dropdown entry"
  {:id s/Any
   :text s/Str
   :type (s/enum :entry)
   (s/optional-key :disabled) s/Bool
   (s/optional-key :url) s/Str})

(def DividerSchema
  "Schema for a dropdown divider"
  {:type (s/enum :divider)})

(def DropdownSchema
  {:items [(s/either EntrySchema DividerSchema)]
   :title s/Str
   (s/optional-key :id) (s/either s/Keyword s/Str s/Int)
   (s/optional-key :korks) (s/either s/Any [s/Any])
   (s/optional-key :icon) s/Str
   (s/optional-key :className) s/Str
   (s/optional-key :on-selection) (s/pred fn?)
   (s/optional-key :as-link?) s/Bool
   (s/optional-key :type) (s/enum :default :menu)
   (s/optional-key :size) (s/enum :default :sm :xs :lg)})


;; ---------------------------------------------------------------------
;; Public

(defmulti dropdown
  (fn [cursor {:keys [id title items type size] :as options
               :or {size :default type :default}}]
    (s/validate DropdownSchema options)
    (:type options)))

(defmethod dropdown :menu [cursor options]
  (om/build dropdown-menu-container cursor {:state options}))

(defmethod dropdown :default [cursor options]
  (om/build dropdown-container cursor {:state options}))
