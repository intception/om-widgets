(ns om-widgets.radiobutton
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [om-widgets.utils :as utils]))


(defn- radio [entity owner]
  (reify
    om/IRenderState
    (render-state [this {:keys [label id path checked-value disabled class-name label-class tabindex]}]
      (dom/div #js {:className class-name}
               (dom/label #js {:className (or label-class "label")}
                          (dom/input #js {:type "radio"
                                          :id id
                                          :disabled disabled
                                          :tabIndex tabindex
                                          :checked (= (utils/om-get entity [path]) checked-value)
                                          :onChange (fn [e]
                                                      (utils/om-update! entity path checked-value)
                                                      (when (utils/atom? entity)
                                                        (om/refresh! owner)))}
                                     label))))))

;; ---------------------------------------------------------------------
;; Public

(defn radiobutton
  [entity path {:keys [label class-name id checked-value disabled label-class tabindex]
                :or {checked-value true class-name "om-widgets-radio"}}]
  (om/build radio entity {:state {:label label
                                  :id id
                                  :tabindex tabindex
                                  :disabled disabled
                                  :class-name class-name
                                  :label-class label-class
                                  :checked-value checked-value
                                  :path path}}))
