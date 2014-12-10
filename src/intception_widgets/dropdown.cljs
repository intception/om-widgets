(ns intception-widgets.dropdown
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
    (display-name[_] "DropdownEntry")

    om/IRenderState
    (render-state [this {:keys [channel] :as state}]
                  (html
                    [:li
                     ;; we use OnMouseDown because onBlour is triggered before
                     ;; onClick event, we use onBlour to close the dropdown
                     [:a (->> {:onMouseDown #(put! channel
                                                   {:type :entry-click
                                                    :value (:id @entry)
                                                    :link (:url @entry)})}
                              (#(if (:url entry)
                                  (merge {:href (:url entry)} %)
                                  %)))
                      (:text entry)]]))))

(defmethod build-entry :divider [entry app]
  (reify
    om/IDisplayName
    (display-name[_] "DropdownDivider")

    om/IRenderState
    (render-state [this state]
                  (html
                    [:li {:class "divider"}]))))

;; ---------------------------------------------------------------------
;; Build class multimethod

(defmulti build-dropdown-class (fn [opened size] size))

(defmethod build-dropdown-class :default [opened size]
  (str "dropdown" (when opened " open")))

(defmethod build-dropdown-class :xs [opened size]
  (str "dropdown btn-group btn-group-xs" (when opened " open")))

(defmethod build-dropdown-class :sm [opened size]
  (str "dropdown btn-group btn-group-sm" (when opened " open")))

(defmethod build-dropdown-class :lg [opened size]
  (str "dropdown btn-group btn-group-lg" (when opened " open")))


;; ---------------------------------------------------------------------
;; Dropdown containers

(defn- build-dropdown-js-options
  [state]
  {:class (build-dropdown-class (:opened state) (:size state))
   :onClick #(put! (:channel state) {:type :open-dropdown})
   :onBlur #(put! (:channel state) {:type :close-dropdown})
   :id (str "dropdown-" (name (:id state)))})

(defn- channel-processing
  [owner]
  (let [channel (om/get-state owner :channel)
        on-selection (om/get-state owner :on-selection)]
    (go-loop []
             (let [msg (<! channel)]
               (condp = (:type msg)
                  :open-dropdown (om/set-state! owner :opened (not (om/get-state owner :opened)))
                  :close-dropdown (om/set-state! owner :opened false)
                  :entry-click (do
                                 (when (:link msg)
                                   (set! (.-location js/window) (:link msg)))
                                 (when on-selection
                                   (on-selection (:value msg)))
                                 (put! channel {:type :close-dropdown})))
               (recur)))))

(defn- DropdownMenu
  [app owner]
  (reify
    om/IDisplayName
       (display-name [_] "DropdownMenu")

    om/IRenderState
    (render-state [this {:keys [channel items] :as state}]
                  (html
                     (vec (concat [:ul {:class "dropdown-menu"}]
                                  (map #(om/build build-entry % {:state {:channel channel}}) items)))))))

;; TODO merge with basic dropdown?
(defn- DropdownMenuContainer [cursor owner]
  (reify
    om/IDisplayName
    (display-name [_] "DropdownMenuContainer")

    om/IWillMount
    (will-mount [_] (channel-processing owner))

    om/IInitState
    (init-state [_]
                {:opened false
                 :channel (chan)})

    om/IRenderState
    (render-state [_ {:keys [title items channel] :as state}]
                  (html
                    [:li (build-dropdown-js-options state)
                     [:a {:class "dropdown-toggle"
                          :title title}
                      title
                      [:span {:class "caret"}]]
                     (om/build DropdownMenu cursor {:state {:channel channel
                                                            :items items}})]))))

;; TODO merge with menu dropdown?
(defn- DropdownContainer [cursor owner]
  (reify
    om/IDisplayName (display-name[_] "DropdownContainer")
    om/IWillMount
    (will-mount [_] (channel-processing owner))

    om/IInitState
    (init-state [_]
                {:opened false
                 :channel (chan)})

    om/IRenderState
    (render-state [_ {:keys [title items channel] :as state}]
                  (html
                    [:div (build-dropdown-js-options state)
                     [:button {:class "btn btn-default dropdown-toggle" :type "button"}
                      (str title " ")
                      [:span {:class "caret"}]]
                     (om/build DropdownMenu cursor {:state {:channel channel
                                                            :items items}})]))))

;; ---------------------------------------------------------------------
;; Schema

(def EntrySchema
  "Schema for a dropdown entry"
  {:id s/Keyword
   :text s/Str
   :type (s/enum :entry)
   (s/optional-key :url) s/Str})

(def DividerSchema
  "Schema for a dropdown divider"
  {:type (s/enum :divider)})

(def DropdownSchema
  {:id s/Keyword
   :items [(s/either EntrySchema DividerSchema)]
   :title s/Str
   (s/optional-key :on-selection) (s/pred fn?)
   (s/optional-key :type) (s/enum :default :menu)
   (s/optional-key :size) (s/enum :default :sm :xs :lg)})


;; ---------------------------------------------------------------------
;; Public


;; TODO we should update the cursor with the clicked value
(defmulti dropdown
  (fn [cursor {:keys [id title items type size] :as options :or {size :default type :default}}]
    (s/validate DropdownSchema options)
    (:type options)))

(defmethod dropdown :menu [cursor options]
  (om/build DropdownMenuContainer cursor {:state options}))

(defmethod dropdown :default [cursor options]
  (om/build DropdownContainer cursor {:state options}))
