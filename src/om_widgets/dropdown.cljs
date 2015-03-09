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

(defmethod build-entry :entry [entry app]
  (reify
    om/IDisplayName
    (display-name [_] "DropdownEntry")

    om/IRenderState
    (render-state [this {:keys [channel] :as state}]
      (html
       [:li
        ;; we use OnMouseDown because onBlour is triggered before
        ;; onClick event, we use onBlour to close the dropdown
        [:a (->> {:onMouseDown #(let [e (if (om/cursor? entry) @entry entry)]
                                  (put! channel {:type :entry-click
                                                 :value (:id e)
                                                 :link (:url e)}))}
                 (#(if (:url entry)
                     (merge {:href (:url entry)} %)
                     %)))
         (:text entry)]]))))

(defmethod build-entry :divider [entry app]
  (reify
    om/IDisplayName
    (display-name [_] "DropdownDivider")

    om/IRenderState
    (render-state [this state]
      (html
       [:li {:class "divider"}]))))

;; ---------------------------------------------------------------------
;; Build class multimethod

(defmulti build-dropdown-class (fn [opened size] size))

(defmethod build-dropdown-class :default [opened size]
  (str "om-widgets-dropdown dropdown" (when opened " open")))

(defmethod build-dropdown-class :xs [opened size]
  (str "om-widgets-dropdown dropdown btn-group btn-group-xs" (when opened " open")))

(defmethod build-dropdown-class :sm [opened size]
  (str "om-widgets-dropdown dropdown btn-group btn-group-sm" (when opened " open")))

(defmethod build-dropdown-class :lg [opened size]
  (str "om-widgets-dropdown dropdown btn-group btn-group-lg" (when opened " open")))


;; ---------------------------------------------------------------------
;; Dropdown containers

(defn- build-dropdown-js-options
  [state]
  {:class (build-dropdown-class (:opened state) (:size state))
   :onClick (fn [e]
              (put! (:channel state) {:type :open-dropdown})
              (when (:prevent-default state)
                (.preventDefault e))
              (when (:stop-propagation state)
                (.stopPropagation e)))
   :onBlur #(put! (:channel state) {:type :close-dropdown})})

(defn- channel-processing
  [cursor owner]
  (let [channel (om/get-state owner :channel)
        on-selection (om/get-state owner :on-selection)
        set-path (om/get-state owner :set-path)]
    (go-loop []
      (let [msg (<! channel)]
        (condp = (:type msg)
          :open-dropdown (om/set-state! owner :opened (not (om/get-state owner :opened)))
          :close-dropdown (om/set-state! owner :opened false)
          :entry-click (do
                         (when (:link msg)
                           (set! (.-location js/window) (:link msg)))
                         (when set-path
                           (om/update! cursor set-path (:value msg)))
                         (when on-selection
                           (on-selection (:value msg)))
                         (put! channel {:type :close-dropdown})))
        (recur)))))

(defn- dropdown-menu
  [cursor owner]
  (reify
    om/IDisplayName
    (display-name [_] "dropdown-menu")

    om/IRenderState
    (render-state [this {:keys [channel items] :as state}]
      (html
       (vec (concat [:ul {:class "dropdown-menu"}]
                    (map #(om/build build-entry % {:state {:channel channel}}) items)))))))

;; TODO merge with basic dropdown?
(defn- dropdown-menu-container [cursor owner]
  (reify
    om/IDisplayName
    (display-name [_] "dropdown-menu-container")

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

;; TODO merge with menu dropdown?
(defn- dropdown-container [cursor owner]
  (reify
    om/IDisplayName
    (display-name [_] "dropdown-container")

    om/IInitState
    (init-state [_]
      {:opened false
       :channel (chan)})

    om/IWillMount
    (will-mount [_] (channel-processing cursor owner))

    om/IRenderState
    (render-state [_ {:keys [title items channel] :as state}]
      (html
       [:div (build-dropdown-js-options state)
        [:button {:class "btn btn-default dropdown-toggle" :type "button"}
         (str title " ")
         [:span {:class "caret"}]]
        (om/build dropdown-menu cursor {:state {:channel channel
                                                :items items}})]))))

;; ---------------------------------------------------------------------
;; Schema

(def EntrySchema
  "Schema for a dropdown entry"
  {:id s/Any
   :text s/Str
   :type (s/enum :entry)
   (s/optional-key :url) s/Str})

(def DividerSchema
  "Schema for a dropdown divider"
  {:type (s/enum :divider)})

(def DropdownSchema
  {:items [(s/either EntrySchema DividerSchema)]
   :title s/Str
   (s/optional-key :id) s/Keyword
   (s/optional-key :set-path) s/Keyword ;; cursor path where we are going to update! selected item
   (s/optional-key :on-selection) (s/pred fn?)
   (s/optional-key :prevent-default) s/Bool
   (s/optional-key :stop-propagation) s/Bool
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
