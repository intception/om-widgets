(ns intception-widgets.radiobutton
  (:require [om.core :as om :include-macros true]
     [om.dom :as dom :include-macros true]
     [intception-widgets.utils :as utils]))


(defn- radio [entity]
 (reify
   om/IRenderState
   (render-state [this {:keys [label id path checked-value disabled class-name label-class]}]
     (dom/div #js {:className class-name}
        (dom/label #js {:className (or label-class "label")}
          (dom/input #js {:type "radio"
                          :id id
                          :disabled disabled
                          :checked (= (utils/om-get entity [path]) checked-value)
                          :onChange (fn [e]
                                      (utils/om-update! entity path checked-value ))} label))))))

(defn radiobutton [entity path  {:keys [label class-name id checked-value disabled label-class]
                                 :or {checked-value true class-name "om-widgets-radio"}}]
  (om/build radio entity {:state {:label label
                                  :id id
                                  :disabled disabled
                                  :class-name class-name
                                  :label-class label-class
                                  :checked-value checked-value
                                  :path path}}))

(defn- group [entity]
  (reify
    om/IRenderState
    (render-state [this {:keys [class-name label-class id path options disabled]}]
      (apply dom/div nil
         (map (fn [[value label]]
                  (radiobutton entity path {:label label
                                            :disabled disabled
                                            :label-class label-class
                                            :checked-value value})) options)))))

(defn radiobutton-group
  [entity path {:keys [options id read-only disabled class-name label-class]
                :or {class-name "om-widgets-radiogroup"}}]
  (om/build group entity {:state {:options options
                                  :class-name class-name
                                  :label-class label-class
                                  :disabled disabled
                                  :id (or id path)
                                  :path path}}))
