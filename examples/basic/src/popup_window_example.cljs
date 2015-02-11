(ns examples.basic.popup-window-example
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [sablono.core :as html :refer-macros [html]]
            [om-widgets.core :as w]))


(defn popup-window-example
  [app owner]
  (reify
    om/IDisplayName
    (display-name [_] "Popup Window Sample")

    om/IRenderState
    (render-state [this state]
      (dom/div #js {:className "panel panel-default"}
        (dom/div #js {:className "panel-heading"} "Popup window sample")
        (dom/div #js {:className "panel-body" :style #js {:overflow "scroll" :height "600"}}
          (w/grid (get-in app [:grid :source-simple])
                  (get-in app [:grid :selected])
                  :container-class-name ""
                  :header {:type :default
                          :columns (get-in app [:grid :columns])})

          (w/popover "Popover con varias palabras que pueden afectarse por word wrapping!"
            (fn [close-window]
                  (dom/div #js {:className "popup-window-sample"}
                    (w/grid (get-in app [:grid :source-simple])
                            (get-in app [:grid :selected])
                            :page-size 4
                            :container-class-name ""
                            :header {:type :default
                                    :columns (get-in app [:grid :columns])})))
            {:prefered-side :bottom
             :align 0})

           (w/popover
            (fn [show-window]
              (dom/button #js {:id "pup" :onClick #(show-window)} "Popup"))

            (fn [close-window]
                  (dom/div #js {:className "popup-window-sample"}
                    (w/grid (get-in app [:grid :source-simple])
                            (get-in app [:grid :selected])
                            :page-size 4
                            :container-class-name ""
                            :header {:type :default
                                    :columns (get-in app [:grid :columns])})
                    (dom/button #js {:onClick #(close-window)} "Close")))
            {:prefered-side :bottom
              :for "pup"} )

           (w/popover
            (fn [show-window]
              (dom/a #js {:id "pup2" :onMouseOver #(show-window)} "Mouse Over!"))

            (fn [close-window]
                  (dom/div #js {:className "popup-window-sample"}
                    (w/grid (get-in app [:grid :source-simple])
                            (get-in app [:grid :selected])
                            :page-size 4
                            :container-class-name ""
                            :header {:type :default
                                    :columns (get-in app [:grid :columns])})
                    (dom/button #js {:onClick #(close-window)} "Close")))
            {:prefered-side :bottom
              :for "pup2"}))))))