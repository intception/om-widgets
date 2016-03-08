(ns examples.basic.datepicker-example
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [sablono.core :as html :refer-macros [html]]
            [om-widgets.core :as w]))


(defn datepicker-example
  [app owner]
  (reify
    om/IRenderState
    (render-state [this state]
      (html
        [:div.panel.panel-default
         [:div.panel-heading "Datepciker"]
         [:div.panel-body
          [:div.row
           [:div.col-lg-6
            [:div.well
             [:label "Input Group - left side"]
             (w/popover
               (fn [show]
                 [:div.input-group
                  [:span.input-group-btn
                   [:button.btn.btn-primary {:id "btn-cal-left" :onClick show}
                    [:span.glyphicon.glyphicon-calendar]]]
                  (w/textinput app :input-group-left {:input-class "form-control"
                                                      :input-format "date"
                                                      :placeholder "MM/DD/YYYY"})])
               (fn [close]
                 (w/datepicker app :input-group-left))
               {:for "btn-cal-left"})]

            [:div.well
             [:label "Input Group - right side"]
             (w/popover
               (fn [show]
                 [:div.input-group
                  (w/textinput app :input-group-right {:input-class "form-control"
                                                       :input-format "date"
                                                       :placeholder "MM/DD/YYYY"})
                  [:span.input-group-btn
                   [:button.btn.btn-primary {:id "btn-cal-right" :onClick show}
                    [:span.glyphicon.glyphicon-calendar]]]])
               (fn [close]
                 (w/datepicker app :input-group-right))
               {:for "btn-cal-right"})]

            [:div.well
             [:label "Input Group - Close when selecting day"]
             (w/popover
               (fn [show]
                 [:div.input-group
                  (w/textinput app :input-group-close-on-change {:input-class "form-control"
                                                                 :input-format "date"
                                                                 :placeholder "MM/DD/YYYY"})
                  [:span.input-group-btn
                   [:button.btn.btn-primary {:id "btn-cal-close-on-select" :onClick show}
                    [:span.glyphicon.glyphicon-calendar]]]])
               (fn [close]
                 (w/datepicker app :input-group-close-on-change {:onChange close}))
               {:for "btn-cal-close-on-select"})]]

           [:div.col-lg-6
            [:div.well
             (w/datepicker app :inline)
             ]
            [:label (str (:inline app))]]]]]))))