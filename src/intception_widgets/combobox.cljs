(ns intception-widgets.combobox
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs.reader :as reader]))

(defn- option
  [[value name]]
  (om/component
   (dom/option #js {:value (pr-str {:value value})} name)))

(defn- combo
  [app]
 (reify
   om/IRenderState
   (render-state [this {:keys [options path] :as state}]
       (let [value (when (get (into {} options) (path app)) (path app))
             opts (->> {:onChange (fn [e]
                                         (let [value (reader/read-string (.. e -target -value))]
                                           (om/update! app
                                                       (:path state)
                                                       (:value value))
                                             (when (:onChange state)
                                               ((:onChange state) (:value value)))))

                      :className (clojure.string/join " " ["combobox"  (:class-name state)
                                                           (when (and (not (:disabled state)) (:read-only state))
                                                            "combobox-readonly")])
                      :disabled (or (:disabled state) (if (:read-only state) true false))
                      :onBlur (:onBlur state)
                      :value (pr-str {:value value})
                      :id (:id state) }
                     (merge (when (:read-only state) {:readOnly true})))]
         (apply dom/select (clj->js opts)
           (apply conj [(dom/option #js { :value (pr-str {:value nil}) :disabled true})]
                       (om/build-all option options)))))))
(defn combobox
  [app path & {:keys [options class-name id read-only disabled onBlur onChange] :or {class-name ""}}]
 ;; entry point
  (om/build combo app {:state {:options options
                               :class-name class-name
                               :read-only read-only
                               :disabled disabled
                               :id id
                               :onBlur onBlur
                               :onChange onChange
                               :path path}}))
