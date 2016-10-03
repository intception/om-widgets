(ns om-widgets.datepicker
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [om-widgets.utils :as utils]
            [cljs.reader :as reader]
            [cljs-time.core :as time]
            [cljs-time.coerce :as timec]))

;; TODO translate
(defonce days-short ["Mo", "Tu", "We", "Th", "Fr", "Sa", "Su"])

(defn- build-previous-month-days [date]
  (let [current-month (time/date-time (time/year date) (time/month date))
        weekday-current-month (time/day-of-week current-month)
        previous-month (time/minus current-month (time/months 1))
        last-day (time/number-of-days-in-the-month previous-month)
        days-to-fill (range (inc (- last-day (dec weekday-current-month))) (inc last-day))]
    (mapv (fn [d] {:day d
                   :month (- 1 (time/month date))
                   :year (time/year date)
                   :belongs-to-month :previous}) days-to-fill)))

(defn- build-current-month-days [date]
  (let [last-day (time/number-of-days-in-the-month date)]
    (mapv (fn [d]
            {:day d
             :month (time/month date)
             :year (time/year date)
             :belongs-to-month :current})
          (range 1 (inc last-day)))))

(defn- build-next-month-days [date]
  (let [current-month (time/date-time (time/year date) (time/month date))
        last-day-number (time/number-of-days-in-the-month current-month)
        last-day (time/date-time (time/year current-month) (time/month current-month) last-day-number)
        weekday-last-day (time/day-of-week last-day)
        weekday-current-month (time/day-of-week current-month)
        days-to-fill (range 1 (inc (- 14 weekday-last-day)))]
    (mapv (fn [d] {:day d
                   :month (+ 1 (time/month date))
                   :year (time/year date)
                   :belongs-to-month :next}) days-to-fill)))

(defn- build-weeks
  "We build a 7x6 matrix representing the 7 days in a week and
  6 weeks in a month view, this view could include days from the
  past month and days from the next month.

  Example:

  [ [{:day 31 :month 1 :year 2014 :belongs-to-month :previous}]
    [{:day 1 :month 2 :year 2014 :belongs-to-month :current}]
    [...]
    [...]
    [...]
  [{:day 1 :month 3 :year 2014 :belongs-to-month :next}] ]
  "
  [date]
  (let [previous-days (build-previous-month-days date)
        currrent-days (build-current-month-days date)
        next-days (build-next-month-days date)
        days (into [] (concat previous-days currrent-days next-days))]
    (subvec (mapv vec (partition 7 days)) 0 6)))

(defn- day-header [day]
  (om/component
   (dom/th #js {:className "dow"} day)))

(defmulti get-date-from-selected-day (fn [previous-date selected-day]
                                       (:belongs-to-month selected-day)))

(defmethod get-date-from-selected-day :current [previous-date selected-day]
  (timec/to-date (time/date-time (time/year previous-date)
                                 (time/month previous-date)
                                 (:day selected-day))))

(defmethod get-date-from-selected-day :previous [previous-date selected-day]
  (timec/to-date (time/date-time (time/year previous-date)
                                 (- (time/month previous-date) 1)
                                 (:day selected-day))))

(defmethod get-date-from-selected-day :next [previous-date selected-day]
  (timec/to-date (time/date-time (time/year previous-date)
                                 (+ (time/month previous-date) 1)
                                 (:day selected-day))))

(defn current-day?
  ([date-node]
   (current-day? date-node (time/date-time (time/now))))
  ([date-node date-time]
   (and (= (:belongs-to-month date-node) :current)
        (= (:day date-node) (time/day date-time))
        (= (:month date-node) (time/month date-time))
        (= (:year date-node) (time/year date-time)))))

(defmulti build-day-class-name
  (fn [day-node date]
    (:belongs-to-month day-node)))

(defmethod build-day-class-name :current [day-node date]
  (if (current-day? day-node (time/date-time date))
    "day active"
    "day"))

(defmethod build-day-class-name :previous [day-node date] "day old")
(defmethod build-day-class-name :next [day-node date] "day new")

(defmulti day-renderer (fn [app owner state]
                         (:type (if (utils/atom? app)
                                  @app
                                  app))))

(defmethod day-renderer :default
  [app owner {:keys [day]}]
  (:day day))

(defn- day-component [app owner]
  (reify
    om/IDisplayName
    (display-name [_] "DatepickerDay")
    om/IRenderState
    (render-state [this {:keys [day date path onChange] :as state}]
      (dom/td #js {:className (build-day-class-name day date)
                   :data-belongs-to-month (:belongs-to-month day)
                   :onClick #(let [date-updated (get-date-from-selected-day date day)]
                               (utils/om-update! app path date-updated)
                               (when onChange (onChange date-updated)))}
              (day-renderer app owner {:day day})))))

(defn- weeks-component [app owner]
  (reify
    om/IDisplayName
    (display-name [_] "DatepickerWeeks")
    om/IRenderState
    (render-state [this {:keys [date path onChange] :as state}]
      (apply dom/tbody nil
             (map (fn [week]
                    (apply dom/tr nil
                           (map (fn [d]
                                  (om/build day-component app {:state {:day d :path path :date date :onChange onChange}})) week)))
                  (build-weeks date))))))

(defn- year-component [app owner]
  (reify
    om/IDisplayName
    (display-name [_] "DatepickerYear")
    om/IRenderState
    (render-state [this {:keys [parent date path] :as state}]
      (dom/input #js {:className "datepicker-year"
                      :placeholder (time/year date)
                      :maxLength 4
                      :value (time/year date)
                      :belongs-to-month "text"
                      :onChange (fn [e]
                                  (let [current-value (.. e -target -value)
                                        current-value-int (js/parseInt current-value)]
                                      ;; TODO what can we do with < 1000 years and still
                                      ;; have some kind of validation?
                                    (when (= 4 (count current-value))
                                      (om/set-state! parent
                                                     :date
                                                     (time/date-time current-value-int
                                                                     (time/month date)
                                                                     (time/day date))))))}))))

(defn- body-component [app owner]
  (reify
    om/IDisplayName
    (display-name [_] "DatepickerBody")
    om/IRenderState
    (render-state [this {:keys [path date onChange] :as state}]
      (dom/div #js {:className "datepicker datepicker-days" :style #js {:display "block"}}
               (dom/table #js {:className "table-condensed"}
                          (dom/thead nil
                                     (dom/tr nil
                       ;; previous month
                                             (dom/th #js {:className "prev"
                                                          :onClick (fn [e]
                                                                     (om/set-state! owner :date (time/minus date (time/months 1))))} "<")
                       ;; current month
                                             (dom/th #js {:colSpan "3"} (utils/get-hr-month date))

                       ;; current year
                                             (dom/th #js {:colSpan "2"}
                                                     (om/build year-component app {:state {:path path :date date :parent owner}}))

                       ;; next month
                                             (dom/th #js {:className "next"
                                                          :onClick (fn [e]
                                                                     (om/set-state! owner :date (time/plus date (time/months 1))))} ">")

                       ;; datepicker body
                                             (apply dom/tr nil
                                                    (om/build-all day-header days-short))
                                             (om/build weeks-component app {:state {:path path :date date :onChange onChange}}))))))))

(defn datepicker
  "Datepicker public API
  the cursor at the current path is updated when
  the user selects a day, otherwise we update the internal component
  state to trigger re-rendering.

  app >> the cursor
  path >> the internal path to update the cursor

  note: we assume today date if the cursor does not have a date
  "
  [app path {:keys [id hidden onChange local-date?] :or {hidden true}}]
  (om/build body-component app {:state {:id id
                                        :hidden hidden
                                        :date (let [cursor-date (utils/om-get app [path])]
                                                (if local-date?
                                                  (time/local-date-time (time/now))
                                                  (if (instance? js/Date cursor-date)
                                                    (time/date-time cursor-date)
                                                    (time/now))))
                                        :path path
                                        :onChange onChange}}))
