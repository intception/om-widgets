(ns examples.basic.core
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs.core.async :refer [put! chan <! alts! timeout close!]]
            [om-widgets.layouts :as layout :include-macros true]
            [om-widgets.core :as w]
            [om-widgets.navbar :as navbar]
            [om-widgets.popover :as popover]
            [sablono.core :as html :refer-macros [html]]
            [om-widgets.grid :refer [row-builder]]
            [examples.basic.state-example :as state]
            [examples.basic.form-example :refer [form-example]]
            [examples.basic.modal-example :refer [modal-example]]
            [examples.basic.datepicker-example :refer [datepicker-example]]
            [examples.basic.popup-window-example :refer [popup-window-example]]
            [examples.basic.grid-example :refer [grid-example grid-link-example grid-custom-row-sample]]
            [examples.basic.dropdown-example :refer [dropdown-example]])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]))


(enable-console-print!)

(defn my-app [app owner]
  (reify
    om/IDisplayName
    (display-name[_] "App")

    om/IRenderState
    (render-state [this {:keys [popover-config chan] :as state}]
      (html
        [:div

        (om/build popover/install-popover! app)

        (w/navbar app
                  :menu-selected
                  {:items (get-in app [:menu-items])
                   :on-selection #(om/update! app :menu-selected %)
                   :brand-image-url "images/logo.png"
                   :brand-image-expanded true
                   :brand-title "Navbar Sample"})

         (condp = (:menu-selected app)
           :form (om/build form-example (get-in app [:form]))
           :dropdown (om/build dropdown-example (get-in app [:dropdown]))
           :datepicker (om/build datepicker-example app)
           :modal (om/build modal-example app)
           :grid (om/build grid-example (get-in app [:grid]))
           :grid-link (om/build grid-link-example (get-in app [:grid]))
           :grid-custom-row (om/build grid-custom-row-sample (get-in app [:grid]))
           :popup-window (om/build popup-window-example app {:state {:chan chan}}))]))))

(defn ^:export examples
  []
  (om/root
    my-app
    state/app-state
    {:target (.getElementById js/document "app")}))
