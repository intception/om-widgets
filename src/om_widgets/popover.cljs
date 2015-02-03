(ns om-widgets.popover
  (:require [om.core :as om :include-macros true]
     [om.dom :as dom :include-macros true]
     [om-widgets.popup-window :as pw]))

(defn- popover-component [_ owner]
 (reify
   om/IDisplayName
      (display-name [_] "PopOver")

   om/IInitState
   (init-state [this]
     {:visible false})
   om/IRenderState
   (render-state [this {:keys [label id disabled class-name visible body]}]
     (dom/div #js {:className "om-widgets-popover-launcher"}
      (dom/a #js {:className class-name
                  :href "#"
                  :type "button"
                  :id id
                  :disabled disabled
                  :onClick #(do
                              (om/set-state! owner :visible true)
                              false)}
                  label
                  (when visible (om/build pw/popup-window-overlay nil {:state {:mouse-down #(om/set-state! owner :visible false)}}))
                  (when visible (om/build pw/popup-window-container nil {:state {:content-fn body :prefered-side :bottom}
                                                                         :opts {:close-fn #(om/set-state! owner :visible false)}})))))))


;; ---------------------------------------------------------------------
;; Public

(defn  popover [label body {:keys [class-name id disabled ]
                            :or {class-name "om-widgets-popover-button"}}]
  (om/build popover-component nil {:state {:label label
                                          :id (or id label)
                                          :disabled disabled
                                          :class-name class-name
                                          :body body}}))
