(ns examples.basic.core
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [intception-widgets.core :as w]))

(enable-console-print!)

(def app-state
  (atom
    {:birth-date #inst "1991-01-25"}))

(defn my-app [app owner]
  (reify
    om/IDisplayName
       (display-name[_] "App")
    om/IInitState
    (init-state [_]
                {:hide-dropdown true})
    om/IRenderState
    (render-state [this state]
                  (dom/div #js {:className "well"}
                    (dom/div #js {:className "input-group"}
                           (dom/span #js {:className "input-group-btn"}
                                     (dom/button #js {:className "btn btn-primary pull-right"
                                                      :type "button"
                                                      :onClick (fn [e]
                                                                 (om/set-state! owner :hide-dropdown (not (:hide-dropdown state))))}
                                                 (dom/span #js {:className "glyphicon glyphicon-calendar"})))
                           (w/datepicker app :birth-date {:hidden (:hide-dropdown state)})
                           (w/textinput app :birth-date {:input-class "form-control"
                                                         :input-format "date"
                                                         :placeholder "MM/DD/YYYY"}))))))

(om/root my-app
         app-state
         {:target (.getElementById js/document "app")})