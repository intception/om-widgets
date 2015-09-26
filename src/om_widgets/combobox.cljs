(ns om-widgets.combobox
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [schema.core :as s :include-macros true]
            [om-widgets.utils :as utils]
            [cljs.reader :as reader]))


(defn- option
  [[value name]]
  (om/component
   (dom/option #js {:value (pr-str {:value value})} name)))

(defn- combo
  [app owner]
  (reify
    om/IRenderState
    (render-state [this {:keys [options path] :as state}]
      (let [value (when (get (into {} options) (utils/om-get app path)) (utils/om-get app path))
            opts (->> {:onChange (fn [e]
                                   (let [value (reader/read-string (.. e -target -value))]
                                     (utils/om-update! app
                                                       (:path state)
                                                       (:value value))
                                     (when (utils/atom? app)
                                       (om/refresh! owner))

                                     (when (:onChange state)
                                       ((:onChange state) (:value value)))

                                     (.preventDefault e)))

                       :className (clojure.string/join " " ["om-widgets-combobox"  (:class-name state)
                                                            (when (and (not (:disabled state)) (:read-only state))
                                                              "om-widgets-combobox-readonly")])
                       :disabled (or (:disabled state) (if (:read-only state) true false))
                       :onBlur (:onBlur state)
                       :value (pr-str {:value value})
                       :id (:id state)}
                      (merge (when (:tabIndex state)) {:tabIndex (:tabIndex state)})
                      (merge (when (:read-only state) {:readOnly true})))]
        (apply dom/select (clj->js opts)
               (apply conj [(dom/option #js {:value (pr-str {:value nil}) :disabled true})]
                      (om/build-all option options)))))))

;; ---------------------------------------------------------------------
;; Schema

(def ComboboxSchema
  {:options [s/Any s/Any]
   (s/optional-key :id) s/Str
   (s/optional-key :class-name) s/Str
   (s/optional-key :onChange) (s/pred fn?)
   (s/optional-key :read-only) s/Bool
   (s/optional-key :disabled) s/Bool
   (s/optional-key :tabIndex) s/Int})

;; ---------------------------------------------------------------------
;; Public
(defn combobox
  [app path {:keys [options class-name id read-only disabled onBlur onChange tabIndex]
             :or {class-name ""}}]
  (om/build combo app {:state {:options options
                               :class-name class-name
                               :read-only read-only
                               :disabled disabled
                               :tabIndex tabIndex
                               :id id
                               :onBlur onBlur
                               :onChange onChange
                               :path path}}))
