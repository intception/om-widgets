(ns intception-widgets.radiobutton
  (:require [om.core :as om :include-macros true]
     [om.dom :as dom :include-macros true]
     [intception-widgets.utils :as utils]))

(defn- radio [entity]
 (reify
   om/IRenderState
   (render-state [this {:keys [label id path checked-value disabled class-name]}]
     (dom/div #js {:className class-name}
        (dom/label #js {:className "label"}
          (dom/input #js {:type "radio"
                          :id id
                          :disabled disabled
                          :checked (= (utils/om-get entity [path]) checked-value)
                          :onChange (fn [e]
                                      (utils/om-update! entity path checked-value ))} label))))))

(defn  radiobutton [entity path  {:keys [label class-name id checked-value disabled ] :or {checked-value true class-name "radio"}}]
 ;; entry point
  (om/build radio entity {:state {:label label
                                :id id
                                :disabled disabled
                                :class-name class-name
                                :checked-value checked-value
                                :path path}}))

(defn- group [entity]
  (reify
    om/IRenderState
    (render-state [this {:keys [class-name id path options disabled]}]
      (apply dom/div nil
         (map (fn [[value label]]
                  (radiobutton entity path {:label label :disabled disabled :checked-value value})) options)))))


(defn radiobutton-group
  [entity path {:keys [options id read-only disabled class-name] :or {class-name "radiogroup"}}]
 ;; entry point
  (om/build group entity {:state {
                               :options options
                               :class-name class-name
                               :disabled disabled
                               :id (or id path)
                               :path path}}))
