(ns intception-widgets.popover
  (:require [om.core :as om :include-macros true]
     [om.dom :as dom :include-macros true]))

(defn- popover-component [_ owner]
 (reify
   om/IInitState
   (init-state [this]
     {:visible false})
   om/IRenderState
   (render-state [this {:keys [label id disabled class-name visible body]}]
     (dom/div #js {:className "popover-launcher"}
      (dom/a #js {:className class-name
                  :href "#"
                  :type "button"
                  :id id
                  :disabled disabled
                  :onClick #(do
                              (om/set-state! owner :visible (not visible))

                              false)
                  } label)

      (when visible
        (dom/div #js {:className "click-handler" :onClick #(do (om/set-state! owner :visible false)
                                                            false)}))

      (when visible
        (apply dom/div #js {:className "popover"
                            :onClick (fn[e]
                                      (println "OnClick !"))}
            (cond
              (fn? body)[(body)]
              (seq? body) body
              :else [body])))))))

(defn  popover [label body {:keys [class-name id disabled ] :or {class-name "popover-button"}}]
 ;; entry point
  (om/build popover-component nil {:state {:label label
                                          :id (or id label)
                                          :disabled disabled
                                          :class-name class-name
                                          :body body
                                          }}))
