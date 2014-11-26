(ns examples.basic.core
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [widgets.layouts :as layout :include-macros true]
            [intception-widgets.core :as w]))

(enable-console-print!)

(def app-state
  (atom
    {:birth-date #inst "1991-01-25"
     :sex :male
     :menu-selected :dashboard
     :menu-items [[{:text "Dashbaord"
                    :id :dashboard
                    :url "#/dashboard"}
                   {:id :getting-started
                    :text "Gettin Started"
                    :url "#/getting-started"}]
                  [{:text "Profile"
                    :right-position true
                    :id :profile
                    :items [{:id :profile
                             :type :entry
                             :text "User profile"
                             :url "#/profile"}
                            {:id :logout
                             :type :entry
                             :text "Logout"
                             :url "#/logout"}]}]]}))

(defn- datepicker-sample
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
                                                                                    :placeholder "MM/DD/YYYY"}))))))))

(defn- radiobutton-sample
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


(defn my-app [app owner]
  (reify
    om/IDisplayName
    (display-name[_] "App")

    om/IRenderState
    (render-state [this state]
                  (dom/div nil
                           (w/navbar app-state {:items (get-in app [:menu-items])
                                                :selected (get-in app [:menu-selected])
                                                :brand-image-url "images/logo.png"
                                                :brand-title "Navbar Sample"})

                           (om/build datepicker-sample app)
                           (om/build radiobutton-sample app)

                           )

                  )))

(om/root my-app
         app-state
         {:target (.getElementById js/document "app")})