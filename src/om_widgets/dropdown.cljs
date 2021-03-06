(ns om-widgets.dropdown
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]
                   [pallet.thread-expr :as th])
  (:require [schema.core :as s :include-macros true]
            [om.core :as om :include-macros true]
            [om-widgets.utils :as u]
            [cljs.core.async :refer [put! chan <! alts! timeout close!]]
            [sablono.core :as html :refer-macros [html]]
            [pallet.thread-expr :as th]))


;; TODO support headings: <li class="dropdown-header">Nav header</li>

;; ---------------------------------------------------------------------
;; Dropdown entry and divider

(defmulti build-entry (fn [entry app] (:type entry)))

(defmethod build-entry :entry
  [entry owner]
  (reify
    om/IRenderState
    (render-state [this {:keys [channel] :as state}]
      (html
        ;; we use OnMouseDown because onBlur is triggered before
        ;; onClick event, we use onBlur to close the dropdown
        [:li (->> {:class (when (:disabled entry) "disabled")}
                  (merge (when-not (:disabled entry)
                           {:onMouseDown #(let [e (if (om/cursor? entry) (om/get-props owner) entry)]
                                           (put! (:channel state) {:type :close-dropdown})
                                           (put! channel {:type :entry-click
                                                          :value (:id e)
                                                          :link (:url e)})
                                           (.preventDefault %)
                                           (.stopPropagation %))})))
         [:a (->> {}
                  (#(if (:url entry)
                     (merge {:href (:url entry)} %)
                     %)))
          (when (:icon entry) [:span {:class (u/glyph (:icon entry))}])
          [:span (:text entry)
           (when (:badge entry) [:span.badge (:badge entry)])]]]))))

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
  [{:keys [size opened channel className] :as state}]
  {:class ["om-widgets-dropdown dropdown btn-group"
           (str "btn-group-" (name (or size :md)))
           (when opened " open")
           (when className className)]
   ;; tab index is set to 0 to force focus on the container,
   ;; this way, the onBlur event will be called when the user
   ;; clicks outside and we can close the dropdown.
   :tabIndex 0
   :onClick (fn [e]
              (put! (:channel state) {:type :open-dropdown})
              (.preventDefault e)
              (.stopPropagation e))
   :onBlur #(do (put! (:channel state) {:type :close-dropdown})
                (.preventDefault %))})

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

(defn- dropdown-menu-container
  [cursor owner]
  (reify
    om/IInitState
    (init-state [_]
      {:opened false
       :channel (chan)})

    om/IWillMount
    (will-mount [_] (channel-processing cursor owner))

    om/IRenderState
    (render-state [_ {:keys [title items icon badge channel] :as state}]
      (html
       [:li (build-dropdown-js-options state)
        [:a (-> {:class "dropdown-toggle"}
                (th/when-> (string? title)
                  (merge {:title title})))
         (when icon [:span {:class (u/glyph icon)}])
         (if (fn? title)
           (title)
           [:div
            [:span title]
            [:span {:class "caret"}]])
         (when badge [:span.badge badge])]
        (om/build dropdown-menu cursor {:state {:channel channel
                                                :items items}})]))))

(defn- dropdown-container
  [cursor owner]
  (reify
    om/IInitState
    (init-state [_]
      {:opened false
       :channel (chan)})

    om/IWillMount
    (will-mount [_] (channel-processing cursor owner))

    om/IRenderState
    (render-state [_ {:keys [title as-link? icon badge items channel class-name] :as state}]
      (let [title (str (when icon "  ") title " ")]
        (html
          [:li (build-dropdown-js-options state)
           (if as-link?
             [:a.btn-link
              (when icon [:span {:class icon}])
              title]

             [:button {:class "btn btn-default dropdown-toggle"
                       :type "button"}
              (when icon [:span {:class (u/glyph icon)}])
              (if (fn? title)
                (title)
                [:span title])
              (when badge [:span.badge badge])
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
   (s/optional-key :icon) (s/either s/Keyword s/Str)
   (s/optional-key :badge) (s/either s/Str s/Int)
   (s/optional-key :disabled) s/Bool
   (s/optional-key :url) s/Str})

(def DividerSchema
  "Schema for a dropdown divider"
  {:type (s/enum :divider)})

(def DropdownSchema
  {:items [(s/either EntrySchema DividerSchema)]
   :title (s/either s/Str (s/pred fn?))
   (s/optional-key :id) (s/either s/Keyword s/Str s/Int)
   (s/optional-key :korks) (s/either s/Any [s/Any])
   (s/optional-key :icon) s/Any
   (s/optional-key :badge) (s/either s/Str s/Int)
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
