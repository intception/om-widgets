(ns examples.basic.editable-list-example
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [sablono.core :as html :refer-macros [html]]
            [om-widgets.core :as w]))

(defn editable-list-example
  [app owner]
  (reify
    om/IDisplayName
    (display-name[_] "EditableListSample")

    om/IRenderState
    (render-state [this state]
      (html
        [:div
         [:div.panel.panel-default
          [:div.panel-heading (str "Editable list view example")]
          [:div.panel-body

           "With strings"
           (w/editable-list-view app :editable-list-with-strings)
           "With dates"
           (w/editable-list-view app :editable-list-with-dates {:input-format "date"})
           "With numbers"
           (w/editable-list-view app :editable-list-with-numbers {:input-format "numeric"})]]]))))




