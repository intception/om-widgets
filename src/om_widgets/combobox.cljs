(ns om-widgets.combobox
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [schema.core :as s :include-macros true]
            [sablono.core :refer-macros [html]]
            [om-widgets.utils :as utils]
            [cljs.reader :as reader]
            [om-widgets.utils :as u]))


(defn- option
  [[k v] owner]
  (reify
    om/IRenderState
    (render-state [_ {:keys [selected]}]
      (let [opts (->> {:value (pr-str {:value k})}
                      (merge (when (= k selected) {:selected 1})))]
        (html
          (if (map? v)
            (u/make-childs [:optgroup {:label k}] (om/build-all option v))
            (dom/option (clj->js opts) v)))))))

(defn- combo
  [app owner]
  (reify
    om/IRenderState
    (render-state [this {:keys [options path] :as state}]
      (let [flatten-opts (reduce (fn [acc [k v]] (conj acc (if (coll? v) v {k v}))) {} options)
            value (when (get flatten-opts (utils/om-get app path)) (utils/om-get app path))
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

                       :className (clojure.string/join " " ["om-widgets-combobox" (:class-name state)
                                                            (when (and (not (:disabled state)) (:read-only state))
                                                              "om-widgets-combobox-readonly")])
                       :disabled (or (:disabled state) (if (:read-only state) true false))
                       :onBlur (:onBlur state)
                       ;:value (pr-str {:value value})
                       :id (:id state)}
                      (merge (when (:tabIndex state)) {:tabIndex (:tabIndex state)})
                      (merge (when (:read-only state) {:readOnly true}))
                      (merge (when (nil? value) {:value (pr-str {:value nil})})))]
        (apply dom/select (clj->js opts)
               ;; create an empty value to override <select> default behaviour of always selecting the first item
               ;; this plays well with required values or form validation
               (apply conj [(dom/option (clj->js (-> {:value (pr-str {:value nil})
                                                      :disabled true}
                                                     (merge (when (nil? value)
                                                              {:selected 1})))))]
                      (om/build-all option options {:state {:selected value}})))))))

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
