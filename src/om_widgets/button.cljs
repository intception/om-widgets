(ns om-widgets.button
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]))


;; TODO this component doesn't do anything, no sense.
(defn- button-component [entity]
  (reify
    om/IRenderState
    (render-state [this {:keys [label id path checked-value disabled class-name onClick]}]
      (dom/button #js {:className class-name
                       :type "button"
                       :id id
                       :disabled disabled
                       :onClick onClick} label))))

(defn  button [label {:keys [class-name id disabled onClick] :or {class-name "om-widgets-button"}}]
 ;; entry point
  (om/build button-component nil {:state {:label label
                                          :id (or id label)
                                          :disabled disabled
                                          :class-name class-name
                                          :onClick onClick}}))
