(ns examples.basic.core
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [om-widgets.layouts :as layout :include-macros true]
            [om-widgets.core :as w]
            [om-widgets.grid :refer [row-builder]]
            [examples.basic.state-example :as state]
            [examples.basic.modal-example :refer [modal-example]]
            [examples.basic.datepicker-example :refer [datepicker-example]]
            [examples.basic.popup-window-example :refer [popup-window-example]]
            [examples.basic.radiobutton-example :refer [radiobutton-example]]
            [examples.basic.grid-example :refer [grid-example grid-custom-row-sample]]
            [examples.basic.dropdown-example :refer [dropdown-example]]))


(enable-console-print!)

(defn my-app [app owner]
  (reify
    om/IDisplayName
    (display-name[_] "App")

    om/IRenderState
    (render-state [this state]
                  (dom/div nil
                           (w/navbar app
                                     :menu-selected
                                     {:items (get-in app [:menu-items])
                                      :on-selection #(println "selected item: " %)
                                      :brand-image-url "images/logo.png"
                                      :brand-image-expanded true
                                      :brand-title "Navbar Sample"})

                           (condp = (:menu-selected app)
                             :dropdown (om/build dropdown-example (get-in app [:dropdown]))
                             :datepicker (om/build datepicker-example app)
                             :modal (om/build modal-example app)
                             :grid (om/build grid-example (get-in app [:grid]))
                             :grid-custom-row (om/build grid-custom-row-sample (get-in app [:grid]))
                             :radiobutton (om/build radiobutton-example app)
                             :popup-window (om/build popup-window-example app))))))

(om/root
  my-app
  state/app-state
  {:target (.getElementById js/document "app")})
