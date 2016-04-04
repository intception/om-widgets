(ns om-widgets.checkbox
  (:require [om.core :as om :include-macros true]
            [schema.core :as s :include-macros true]
            [sablono.core :refer-macros [html]]
            [om-widgets.utils :as utils]))


(defn- checked?
  [dest checked-value]
  (if (set? dest)
    (contains? dest checked-value)
    (= checked-value dest)))

(defn- check
  [app owner]
  (reify
    om/IRenderState
    (render-state [this {:keys [label id class-name title path disabled on-change checked-value unchecked-value toggle-value]}]
      (html
        [:div (-> {:class [class-name
                           (when disabled "disabled")
                           (when (checked? (utils/om-get app path) checked-value) "active")]}
                  (merge (when title) {:title title}))
         [:label {:class ["om-widgets-label"
                          (when disabled "text-muted")]
                  :htmlFor (name id)}
          [:input {:type "checkbox"
                   :id (name id)
                   :disabled disabled
                   :checked (checked? (utils/om-get app path) checked-value)
                   :onChange (fn [e]
                               (let [value (if (.. e -target -checked) checked-value unchecked-value)
                                     dest (get @(om/get-props owner) path)]

                                 (if toggle-value
                                   (if (contains? dest checked-value)
                                     (utils/om-update! (om/get-props owner) path (disj dest checked-value))
                                     (utils/om-update! (om/get-props owner) path (conj dest checked-value)))
                                   (utils/om-update! (om/get-props owner) path value))

                                 ;; TODO this is done to force a refresh
                                 (when (utils/atom? (om/get-props owner))
                                   (om/set-state! owner ::force-refresh (not (om/get-state owner ::force-refresh))))

                                 (when on-change (on-change value))))}]
          label]]))))

;; ---------------------------------------------------------------------
;; Schema

(def CheckboxSchema
  {(s/optional-key :label) s/Any
   (s/optional-key :title) s/Str
   (s/optional-key :checked-value) s/Any
   (s/optional-key :id) s/Str
   (s/optional-key :disabled) s/Bool
   (s/optional-key :class-name) s/Str
   (s/optional-key :on-change) (s/pred fn?)
   (s/optional-key :onChange) (s/pred fn?)

   (s/optional-key :unchecked-value) s/Any
   (s/optional-key :toggle-value) s/Bool
   (s/optional-key :tabIndex) s/Int})

;; ---------------------------------------------------------------------
;; Public
(defn checkbox
  [app path {:keys [label id title disabled class-name on-change onChange checked-value unchecked-value toggle-value] :as opts
             :or {class-name "checkbox"
                  checked-value true
                  unchecked-value false
                  toggle-value false}}]
  (om/build check app {:state {:label label
                               :title title
                               :id (or id path)
                               :class-name class-name
                               :checked-value checked-value
                               :unchecked-value unchecked-value
                               :toggle-value toggle-value
                               :disabled disabled
                               :on-change (or on-change onChange)
                               :path path}}))
