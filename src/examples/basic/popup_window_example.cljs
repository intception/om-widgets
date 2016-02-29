(ns examples.basic.popup-window-example
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs.core.async :refer [put! chan <! alts! timeout close!]]
            [sablono.core :refer-macros [html]]
            [om-widgets.core :as w]))


(defn popup-window-example
  [app owner]
  (reify
    om/IInitState
    (init-state [_]
      {:channel (chan)})

    om/IRenderState
    (render-state [this state]

      (html
        [:div.panel.panel-default
         [:div.panel-heading "Popup window sample"]
         [:div.panel-body
          [:div.row
           [:div.col-lg-3
            (w/popover
              (fn [show-window]
                [:button.btn.btn-default {:id "popup-bottom-sample" :onClick #(show-window)} "▼ Popup Bottom Position"])

              (fn [close-window]
                (html
                  [:div.jumbotron
                   [:h3 "Click outside to close!"]]))
              {:prefered-side :bottom
               :popover-class ""
               :for "popup-bottom-sample"})]

           [:div.col-lg-3
            (w/popover
              (fn [show-window]
                [:button.btn.btn-default {:id "popup-top-sample" :onClick #(show-window)} "▲ Popup Top Position"])

              (fn [close-window]
                (html
                  [:div.jumbotron
                   [:h3 "Click outside to close!"]]))
              {:prefered-side :top
               :popover-class ""
               :for "popup-top-sample"})]

           [:div.col-lg-3
            (w/popover
              (fn [show-window]
                [:button.btn.btn-default {:id "popup-left-sample" :onClick #(show-window)} "◀ Popup Left Position"])

              (fn [close-window]
                (html
                  [:div.jumbotron
                   [:h3 "Click outside to close!"]]))
              {:prefered-side :left
               :popover-class ""
               :for "popup-left-sample"})]

           [:div.col-lg-3
            (w/popover
              (fn [show-window]
                [:button.btn.btn-default {:id "popup-right-sample" :onClick #(show-window)} "► Popup Right Position"])

              (fn [close-window]
                (html
                  [:div.jumbotron
                   [:h3 "Click outside to close!"]]))
              {:prefered-side :right
               :popover-class ""
               :for "popup-right-sample"})]]

          [:hr]

          (w/popover
            (fn [show-window]
              [:button.btn.btn-default {:id "popup-close-btn-sample" :onClick #(show-window)} "With close button"])

            (fn [close-window]
              (html
                [:div.jumbotron
                 [:h3 "Click button to close!"]
                 [:button.btn.btn-danger {:onClick #(close-window)} "Close"]]))
            {:prefered-side :right
             :popover-class ""
             :for "popup-close-btn-sample"})

          [:hr]

          (w/popover
            "As simple link button"
            (fn [_]
              (html
                [:div.jumbotron
                 [:h3 "Click outside to close!"]]))
            {:prefered-side :right
             :label ""
             :popover-class ""
             :for "popup-close-btn-sample"})

          [:hr]

          (w/popover
            (fn [show-window]
              (dom/a #js {:id "popup-mouse-over" :onMouseOver #(show-window)} "Mouse Over!"))

            (fn [close-window]
              (html
                [:div.popup-window-sample
                 [:h1 "Sample"]
                 [:button {:onClick #(close-window)} "Close"]]))
            {:prefered-side :bottom
             :popover-class ""
             :for "popup-mouse-over"})]]))))