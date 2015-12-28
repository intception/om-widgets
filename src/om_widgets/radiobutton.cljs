(ns om-widgets.radiobutton
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [om-widgets.utils :as utils]))


(defn- radio
  [entity owner]
  (reify
    om/IRenderState
    (render-state [this {:keys [label id path checked-value disabled class-name label-class tabIndex onChange]}]
      (dom/div #js {:className (str class-name (if (= (utils/om-get entity [path]) checked-value) " active" ""))}
               (dom/label #js {:className (str (or label-class "")
                                               (when disabled
                                                 " text-muted "))}
                          (dom/input #js {:type "radio"
                                          :id id
                                          :disabled disabled
                                          :tabIndex tabIndex
                                          :checked (= (utils/om-get entity [path]) checked-value)
                                          :onChange (fn [e]
                                                      (when onChange
                                                        (onChange checked-value))
                                                      (utils/om-update! entity path checked-value)
                                                      (when (utils/atom? entity)
                                                        (om/refresh! owner)))}
                                     label))))))

(defn- group
  [entity]
  (reify
    om/IRenderState
    (render-state [this {:keys [path options disabled class-name label-class]}]
      (apply dom/div nil
             (map (fn [option]
                    (om/build radio entity {:state {:label (:label option)
                                                    :tabIndex (:tabIndex option)
                                                    :disabled disabled
                                                    :class-name class-name
                                                    :label-class label-class
                                                    :onChange (:onChange option)
                                                    :checked-value (:checked-value option)
                                                    :path path}}))
                  options)))))


;; ---------------------------------------------------------------------
;; Public

(defn radiobutton
  [entity path {:keys [label class-name id checked-value disabled label-class tabIndex onChange]
                :or {checked-value true class-name "om-widgets-radio"}}]
  (om/build radio entity {:state {:label label
                                  :id id
                                  :tabIndex tabIndex
                                  :disabled disabled
                                  :class-name class-name
                                  :label-class label-class
                                  :onChange onChange
                                  :checked-value checked-value
                                  :path path}}))

(defn radiobutton-group
  [entity path {:keys [options disabled class-name label-class]}]
  (om/build group entity {:state {:options options
                                  :disabled disabled
                                  :class-name class-name
                                  :label-class label-class
                                  :path path}}))
