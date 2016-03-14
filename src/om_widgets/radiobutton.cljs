(ns om-widgets.radiobutton
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [sablono.core :refer-macros [html]]
            [om-widgets.utils :as utils]))


(defn- radio
  [entity owner]
  (reify
    om/IRenderState
    (render-state [this {:keys [label id path checked-value disabled class-name label-class tabIndex onChange]}]
      (html
        [:div {:class ["radio" class-name (if (= (utils/om-get entity [path]) checked-value) "active")]}
         [:label {:class [label-class (when disabled "text-muted")]}
          [:input {:type "radio"
                   :id id
                   :disabled disabled
                   :tabIndex tabIndex
                   :checked (= (utils/om-get entity [path]) checked-value)
                   :onChange (fn [e]
                               (when onChange
                                 (onChange checked-value))
                               (utils/om-update! (om/get-props owner) path checked-value)
                               (when (utils/atom? (om/get-props owner))
                                 (om/refresh! owner)))}]
          label]]))))

(defn- group
  [entity]
  (reify
    om/IRenderState
    (render-state [this {:keys [path options disabled class-name label-class group-class]}]
      (html
        (utils/make-childs
          [:div {:class group-class}]
          (map (fn [option]
                 (om/build radio entity {:state {:label (:label option)
                                                 :tabIndex (:tabIndex option)
                                                 :disabled disabled
                                                 :class-name class-name
                                                 :label-class label-class
                                                 :onChange (:onChange option)
                                                 :checked-value (:checked-value option)
                                                 :path path}}))
               options))))))


;; ---------------------------------------------------------------------
;; Public

(defn radiobutton
  [entity path {:keys [label class-name id checked-value disabled label-class tabIndex onChange]
                :or {checked-value true
                     class-name "om-widgets-radio"}}]
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
  [entity path {:keys [options disabled class-name label-class group-class]
                :or {group-class "om-widgets-radio-group"}}]
  (om/build group entity {:state {:options options
                                  :group-class group-class
                                  :disabled disabled
                                  :class-name class-name
                                  :label-class label-class
                                  :path path}}))
