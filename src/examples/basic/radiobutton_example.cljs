(ns examples.basic.radiobutton-example
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [sablono.core :as html :refer-macros [html]]
            [om-widgets.core :as w]))


(defn radiobutton-example
  [app owner]
  (reify
    om/IInitState
    (init-state [_]
                {:hide-dropdown true})

    om/IDisplayName
    (display-name[_] "RadioButtonSample")

    om/IRenderState
    (render-state [this state]
                  (dom/div #js {:className "panel panel-default"}
                           (dom/div #js {:className "panel-heading"} (str "RadioButton"
                                                                          " (cursor value "
                                                                          (get-in app [:sex])
                                                                          " )"))
                           (dom/div #js {:className "panel-body"}
                                    (dom/div #js {:className "well"}
                                             (dom/div #js {:className "row"}
                                                      (dom/div #js {:className "col-md-6 col-sm-6"}
                                                               (w/radiobutton app :sex {:checked-value :male
                                                                                        :name "options"
                                                                                        :class-name "some-container-class"
                                                                                        :label-class "some-label-class"
                                                                                        :data-toggle "buttons"
                                                                                        :label " Male"
                                                                                        :id "male"}))
                                                      (dom/div #js {:className "col-md-6 col-sm-6"}
                                                               (w/radiobutton app
                                                                              :sex
                                                                              {:checked-value :female
                                                                               :name "options"
                                                                               :class-name "some-container-class"
                                                                               :label-class "some-label-class"
                                                                               :data-toggle "buttons"
                                                                               :label " Female"
                                                                               :id "female"})))))))))