(ns intception-widgets.dropdown
  (:require [schema.core :as s :include-macros true]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]))

;; TODO support headings: <li class="dropdown-header">Nav header</li>


;; ---------------------------------------------------------------------
;; Dropdown entry and divider

(defmulti build-entry (fn [entry app] (:type entry)))

(defmethod build-entry :entry [entry app]
  (reify
    om/IDisplayName
    (display-name[_] "DropdownEntry")

    om/IRenderState
    (render-state [this {:keys [on-selection] :as state}]
                  (dom/li nil
                          (dom/a (cljs.core/clj->js (->> {}
                                                         (merge (when (:url entry)
                                                                  {:href (:url entry)}))
                                                         (merge (when on-selection
                                                                  {:onClick #(on-selection (:id @entry))}))
                                                         ))
                            (:text entry))))))

(defmethod build-entry :divider [entry app]
  (reify
    om/IDisplayName
    (display-name[_] "DropdownDivider")

    om/IRenderState
    (render-state [this state]
                  (dom/li #js {:className "divider"}))))


;; ---------------------------------------------------------------------
;; Build class multimethod

(defmulti build-dropdown-class (fn [_ size] size))

(defmethod build-dropdown-class :default [opened _]
  (str "dropdown" (when opened " open")))

(defmethod build-dropdown-class :xs [opened _]
  (str "dropdown btn-group-xs" (when opened " open")))

(defmethod build-dropdown-class :sm [opened _]
  (str "dropdown btn-group-sm" (when opened " open")))

(defmethod build-dropdown-class :lg [opened _]
  (str "dropdown btn-group-lg" (when opened " open")))


;; ---------------------------------------------------------------------
;; Dropdown containers

(defn- dropdown-menu-container [app owner]
  (reify
    om/IDisplayName
    (display-name[_] "Dropdown")

    om/IInitState
    (init-state [_]
                {:opened false})

    om/IRenderState
    (render-state [_ {:keys [id title items type size opened on-selection] :as state}]
                  (dom/li #js {:className (build-dropdown-class opened size)
                               :onClick #(om/set-state! owner :opened (not opened))
                               :onBlur #(om/set-state! owner :opened false)
                               :id (str "dropdown-" (name id))}

                          (dom/a #js {:className "dropdown-toggle"
                                      :data-toggle "dropdown"
                                      :aria-expanded "false"
                                      :role "button"}
                                 title
                                 (dom/span #js {:className "caret"}))

                          (apply dom/ul #js {:className "dropdown-menu"
                                             :aria-labelledby (str "dropdown-" (name id))
                                             :role "menu"}
                                 (om/build-all build-entry
                                               (:items state)
                                               {:state {:on-selection on-selection}}))))))

(defn- dropdown-container [app owner]
  (reify
    om/IDisplayName
    (display-name[_] "Dropdown")

    om/IInitState
    (init-state [_]
                {:opened false})

    om/IRenderState
    (render-state [_ {:keys [id title items type size opened on-selection] :as state}]
                  (dom/div #js {:className (build-dropdown-class opened size)
                                :onClick #(om/set-state! owner :opened (not opened))
                                :onBlur #(om/set-state! owner :opened false)
                                :id (str "dropdown-" (name id))}

                           (dom/button #js {:className "btn btn-default dropdown-toggle"
                                            :type "button"
                                            :data-toggle "dropdown"}
                                       title
                                       (dom/span #js {:className "caret"}))

                           (apply dom/ul #js {:className "dropdown-menu"
                                              :aria-labelledby (str "dropdown-" (name id))
                                              :role "menu"}
                                  (om/build-all build-entry
                                                (:items state)
                                                {:state {:on-selection on-selection}}))))))


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

(defmulti dropdown
  (fn [app {:keys [id title items type size] :as options :or {size :default type :default}}]
    (s/validate DropdownSchema options)
    (:type options)))

(defmethod dropdown :menu [app options]
  (om/build dropdown-menu-container app {:state options}))

(defmethod dropdown :default [app options]
  (om/build dropdown-container app {:state options}))
