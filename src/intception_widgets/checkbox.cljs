(ns intception-widgets.checkbox
  (:require [om.core :as om :include-macros true]
     [om.dom :as dom :include-macros true]
     [intception-widgets.utils :as utils]))

(defn- check [app]
 (reify
   om/IRenderState
   (render-state [this {:keys [label id path disabled class-name]}]
     (dom/div #js {:className class-name}
        (dom/label #js {:className "label"}
          (dom/input #js {:type "checkbox"
                          :id id
                          :disabled disabled
                          :checked (if (utils/om-get app [path]) true false)
                          :onChange (fn [e]
                                      (utils/om-update! app path (.. e -target -checked)))}
                     label))))))

(defn checkbox [app path & {:keys [label id disabled class-name] :or {class-name "checkbox"}}]
 ;; entry point
  (om/build check app { :state {:label label
                                :id (or id path)
                                :class-name class-name
                                :disabled disabled
                                :path path}} ))

