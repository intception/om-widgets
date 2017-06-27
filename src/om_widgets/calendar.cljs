(ns om-widgets.calendar
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [om-widgets.utils :as utils]
            [sablono.core :as html :refer-macros [html]]
            [cljs.reader :as reader]
            [cljs-time.core :as time]
            [cljs-time.coerce :as timec])
  (:use [om-widgets.datepicker :only [weeks-component day-header days-short week-component]]))

(defn calendar
  "Calendar does not support date selection nor shows a specific date either"
  [app owner]
  (reify
    om/IInitState
    (init-state [_]
      {:date (time/now)})

    om/IRenderState
    (render-state [_ {:keys [date path id on-month-changed] :as state}]
      (letfn [(change-date [d]
                (om/set-state! owner :date d)
                (when on-month-changed
                  (on-month-changed (timec/to-date
                                     (time/date-time (time/year d)
                                                     (time/month d)
                                                     (time/day d))))))]
        (html
         [:div {:id id}
          [:div {:class "box-header"}
           [:a {:onClick #(change-date (time/minus date (time/months 1)))}
            [:span {:class "icn-big-left-arrow"}]]
           [:h2
            (str  (utils/get-hr-month date) " " (time/year date))]
           [:a {:onClick #(change-date (time/plus date (time/months 1)))}
            [:span {:class "icn-big-right-arrow"}]]]
          [:div {:class "calendar-content"}
           [:table
            [:thead
             (utils/make-childs [:tr]
                                (map #(om/build day-header %) days-short))]
            (om/build weeks-component app
                      {:state {:path path :date date}})]]])))))



(defn week-calendar
  "Calendar with only one week shown"
  [app owner]
  (reify
    om/IInitState
    (init-state [_]
      {:date (time/now)})

    om/IRenderState
    (render-state [_ {:keys [date path id on-week-changed] :as state}]
      (letfn [(change-date [d]
                (om/set-state! owner :date d)
                (when on-week-changed
                  (on-week-changed (timec/to-date
                                    (time/date-time (time/year d)
                                                    (time/month d)
                                                    (time/day d))))))]
        (html
         [:div {:id id}
          [:div {:class "box-header"}
           [:a {:onClick #(change-date (time/minus date (time/weeks 1)))}
            [:span {:class "icn-big-left-arrow"}]]
           [:h2
            (str  (utils/get-hr-month date) " " (time/year date))]
           [:a {:onClick #(change-date (time/plus date (time/weeks 1)))}
            [:span {:class "icn-big-right-arrow"}]]]
          [:div {:class "calendar-content"}
           [:table
            [:thead
             (utils/make-childs [:tr]
                                (map #(om/build day-header %) days-short))]
            (om/build week-component app
                      {:state {:path path :date date}})]]])))))
