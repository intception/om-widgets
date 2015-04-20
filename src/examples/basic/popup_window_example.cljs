(ns examples.basic.popup-window-example
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [sablono.core :as html :refer-macros [html]]
            [cljs.core.async :refer [put! chan <! alts! timeout close!]]
            [om-widgets.core :as w]))


(defn popup-window-example
  [app owner]
  (reify
    om/IDisplayName
    (display-name [_] "Popup Window Sample")

    om/IDidMount
    (did-mount [this]
      (om/set-state! owner :popover-target (om.core/get-node owner)))

    om/IRenderState
    (render-state [this {:keys [chan popover-target]}]
      (dom/div #js {:className "panel panel-default"}
        (dom/div #js {:className "panel-heading"} "Popup window sample")
        (dom/div #js {:className "panel-body" :style #js {:overflow "scroll" :height "600"}}

                 (w/popover (fn [show]
                              (dom/button #js {:onClick #(show)
                                               :id "open"} "ABRIR"))
                            (fn [hide]

                              (dom/button #js {:onClick #(hide)
                                               :id "close"} "CERRRAR"))
                            {:for "open"})

                 (w/popover "swaewqae"
                            (fn [hide]
                              (dom/button #js {:onClick #(hide)
                                               :id "close"} "CERRRAR"))
                            )


               )))))


