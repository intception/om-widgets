(ns examples.basic.datepicker-example
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [sablono.core :as html :refer-macros [html]]
            [om-widgets.core :as w]))


(defn datepicker-example
  [app owner]
  (reify
    om/IInitState
    (init-state [_]
                {:hide-dropdown true})

    om/IDisplayName
    (display-name[_] "DatepickerSample")

    om/IRenderState
    (render-state [this state]
                  (dom/div #js {:className "panel panel-default"}
                           (dom/div #js {:className "panel-heading"} "Datepicker")
                           (dom/div #js {:className "panel-body"}
                                    (dom/div #js {:className "well"}
                                             (w/popover
                                               (fn [show]
                                                 (dom/div #js {:className "input-group"}
                                                          (dom/span #js {:className "input-group-btn"}
                                                                    (dom/button #js {:className "btn btn-primary pull-right"
                                                                                     :type "button"
                                                                                     :id "btn-cal"
                                                                                     :onClick (fn [e]
                                                                                                (show))}
                                                                                (dom/span #js {:className "glyphicon glyphicon-calendar"})))
                                                          (w/textinput app :birth-date {:input-class "form-control"
                                                                                        :input-format "date"

                                                                                        :placeholder "MM/DD/YYYY"})))
                                               (fn [close]
                                                 (w/datepicker app :birth-date ))
                                               {:for "btn-cal"})))))))