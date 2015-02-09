(ns om-widgets.checkbox
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [om-widgets.utils :as utils]))

(defn- check [app owner]
  (reify
    om/IRenderState
    (render-state [this {:keys [label id path disabled on-change class-name]}]
      (dom/div #js {:className class-name}
               (dom/label #js {:className "om-widgets-label"}
                          (dom/input #js {:type "checkbox"
                                          :id id
                                          :disabled disabled
                                          :checked (if (utils/om-get app path) true false)
                                          :onChange (fn [e]
                                                      (utils/om-update! app path (.. e -target -checked))
                                                      (when (utils/atom? app)
                                                        (om/set-state! owner :x (not (om/get-state owner :x)))) ;;om/refresh! ??? don't work !
                                                      (when on-change (on-change (.. e -target -checked))))}
                                     label))))))

(defn checkbox [app path {:keys [label id disabled class-name on-change] :or {class-name "om-widgets-checkbox"}}]
 ;; entry point
  (om/build check app {:state {:label label
                               :id (or id path)
                               :class-name class-name
                               :disabled disabled
                               :on-change on-change
                               :path path}}))

