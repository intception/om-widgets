(ns examples.basic.core
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [om-widgets.layouts :as layout :include-macros true]
            [om-widgets.core :as w]
            [om-widgets.navbar :as navbar]
            [sablono.core :as html :refer-macros [html]]
            [om-widgets.grid :refer [row-builder]]
            [examples.basic.state-example :as state]
            [examples.basic.tab-example :refer [tab-example]]
            [examples.basic.form-example :refer [form-example]]
            [examples.basic.modal-example :refer [modal-example]]
            [examples.basic.datepicker-example :refer [datepicker-example]]
            [examples.basic.popup-window-example :refer [popup-window-example]]
            [examples.basic.grid-example :refer [grid-example grid-link-example grid-custom-row-sample]]
            [examples.basic.dropdown-example :refer [dropdown-example]]))



(enable-console-print!)

(defn my-app [app owner]
  (reify
    om/IDisplayName
    (display-name[_] "App")

    om/IRenderState
    (render-state [this state]
      (html
        [:div
         (w/navbar app
                   :menu-selected
                   {:items (get-in app [:menu-items])
                    :on-selection #(om/update! app :menu-selected %)
                    :brand-image-url "images/logo.png"
                    :brand-image-expanded true
                    :brand-title "Navbar Sample"})
         (condp = (:menu-selected app)
           :tab (om/build tab-example (get-in app [:tab]))
           :form (om/build form-example (get-in app [:form]))
           :dropdown (om/build dropdown-example (get-in app [:dropdown]))
           :datepicker (om/build datepicker-example app)
           :modal (om/build modal-example app)
           :grid (om/build grid-example (get-in app [:grid]))
           :grid-link (om/build grid-link-example (get-in app [:grid]))
           :grid-custom-row (om/build grid-custom-row-sample (get-in app [:grid]))
           :users (html [:div.jumbotron [:h2 "Users Sample"]])
           :inbox (html [:div.jumbotron [:h2 "Inbox Sample"]])
           :popup-window (om/build popup-window-example app))]))))

(defn ^:export examples
  []
  (om/root
    my-app
    state/app-state
    {:target (.getElementById js/document "app")}))
