(ns om-widgets.grid
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [om-widgets.utils :as utils]
            [cljs-time.core :as time]
            [cljs-time.format :as timef]
            [sablono.core :as html :refer-macros [html]]
            [schema.core :as s :include-macros true]
            [cljs.core.async :refer [put! chan <! alts! timeout close!]]
            [om-widgets.translations :refer [translate]]
            [om-widgets.utils :as u]))


(defprotocol ISortableColumnCaret
  (column-caret [_ column sort-info channel]))

(defprotocol ISortableColumnSortData
  (sort-data [_ sort-info rows]))

(defn standard-sort-caret
  [_ owner]
  (om/component
    (let [sort-info (om/get-state owner :sort-info)
          column (om/get-state owner :column)]
      (html [:div {:class "om-widgets-sortable-column"
                   :onClick (fn []
                              (let [sort-info (om/get-state owner :sort-info)
                                    column (if (satisfies? IDeref (om/get-state owner :column))
                                             @(om/get-state owner :column)
                                             (om/get-state owner :column))]
                                (put! (om/get-state owner :channel)
                                      {:sort-info {:column column
                                                   :direction (if (and sort-info
                                                                       (= (:column sort-info)
                                                                          column)
                                                                       (= (:direction sort-info)
                                                                          :up))
                                                                :down
                                                                :up)}})))}
             (:caption column)
             [:span {:class (str "pull-right glyphicon om-widgets-sortable-"
                                 (if (and sort-info
                                          (= (:column sort-info)
                                             column))
                                   (name (:direction sort-info))
                                   "both"))}]]))))

(defn standard-sort [column]
  (reify
    ISortableColumnCaret
    (column-caret [this column sort-info channel]
      (om/build standard-sort-caret nil {:state {:column column
                                                 :sort-info sort-info
                                                 :channel channel}}))
    ISortableColumnSortData
    (sort-data [this sort-info rows]
      (let [direction (:direction sort-info)
            field (get-in sort-info [:column :field])
            sort-fn (get-in sort-info [:column :sort-fn])]
        (-> (if-not sort-fn
              (sort-by #(let [v (get % field)]
                         (if (string? v)
                           (clojure.string/lower-case v)
                           v))
                       rows)
              (sort sort-fn rows))
            (#(if (= :down direction)
               (reverse %)
               %)))))))

(defmulti grid-sorter :sort)

(defmethod grid-sorter :default
  [column])

(defmethod grid-sorter true
  [column]
  (standard-sort column))

;; ---------------------------------------------------------------------
;; TODOS
;;
;; * rethink markup, why we use two tables instead of one?
;;
;; * if you call the grid with an empty vec source, row-builder is called a lot
;; of times with garbage ({:row -1, :row-type nil, :projection {}, :class success})
;;
;; * inconsistence between row-builder and grid-header multimethods, one needs to return
;; a valid om component and the other one is just a function that makes the build.

(defn- title-header-cell [{:keys [caption col-span] :as h} owner]
  (om/component
    (let [sorter (grid-sorter h)]
      (html [:th (when col-span {:colSpan col-span})
             (if (and sorter
                      (satisfies? ISortableColumnCaret sorter))
               (column-caret sorter
                             h
                             (om/get-state owner :sort-info)
                             (om/get-state owner :channel))
               caption)]))))

(defn- header [columns owner]
  (om/component
    (dom/thead #js {:className "om-widgets-title-header-row"}
               (apply dom/tr nil
                      (om/build-all title-header-cell columns {:state {:channel (om/get-state owner :channel)
                                                                       :sort-info (om/get-state owner :sort-info)}})))))

(defn build-page-boundaries
  "Given the pagination information will return where the
  current page starts and ends, usefull to show this info in labels à la gmail (1-10 of 100)

  Note: This function does not assume 0 indexes, so first page is 1 and so on."
  [{:keys [current-page page-size total-items]}]
  (let [end (* current-page page-size)]
    {:start (if (pos? total-items)
              (-> (- end page-size) (+ 1))
              0)
     :end (if (> end total-items)
            total-items
            end)}))

(defn- default-pager
  [pager-definition owner {:keys [language] :as opts}]
  (reify
    om/IDisplayName
    (display-name [_] "GridPager")

    om/IRenderState
    (render-state [this {:keys [current-page max-pages total-rows
                                page-size current-page-total] :as state}]
      (let [page-info (build-page-boundaries {:current-page (inc current-page)
                                              :page-size page-size
                                              :total-items total-rows})
            previous-disabled? (or (= 0 current-page)
                                   (= 0 current-page-total))
            next-disabled? (or (= 0 total-rows)
                               (= current-page max-pages))]
        (dom/ul #js {:className "pager"}
                ;; previous page
                (dom/li #js {:className (when previous-disabled? "disabled")}
                        (dom/a #js {:onClick #(when (> current-page 0)
                                               (put! (:channel state) {:new-page (dec current-page)})
                                               false)}
                               (translate language :grid.pager/previous-page)))
                ;; next page
                (dom/li #js {:className (when next-disabled? "disabled")}
                        (dom/a #js {:onClick #(when (< current-page max-pages)
                                               (put! (:channel state) {:new-page (inc current-page)})
                                               false)}
                               (translate language :grid.pager/next-page)))

                ;; total label
                (dom/span #js {:className "pull-right"}
                          (u/format (translate language :grid.pager/total-rows)
                                    (:start page-info)
                                    (:end page-info)
                                    total-rows)))))))



(defmulti cell-builder (fn [cell owner opts]
                         (:data-format (:column-def opts))))

(defmethod cell-builder :date [date owner opts]
  (let [col-span (:col-span (:column-def opts))
        date-formatter (:date-formatter (:column-def opts))]
    (om/component
      (html
        [:td (-> {:class "om-widgets-data-cell"}
                 (merge (when col-span) {:colSpan col-span}))
         (if date
           (timef/unparse (timef/formatter (or date-formatter
                                               "yyyy/MM/dd"))
                          (time/date-time date))
           "")]))))

(defmethod cell-builder :keyword [cell owner opts]
  (let [col-span (:col-span (:column-def opts))
        options (:options (:column-def opts))]
    (om/component
      (html
        [:td (-> {:class "om-widgets-data-cell"}
                 (merge (when col-span) {:colSpan col-span}))
         (or (get options cell)
             cell)]))))

(defmethod cell-builder :dom [cell owner opts]
  (let [col-span (:col-span (:column-def opts))
        content (:fn (:column-def opts))]
    (om/component
      (html
        [:td (-> {:class "om-widgets-data-cell"}
                 (merge (when col-span) {:colSpan col-span}))
         (content cell (:row opts))]))))

(defmethod cell-builder :default [cell owner opts]
  (let [col-span (:col-span (:column-def opts))]
    (om/component
      (html
        [:td (-> {:class "om-widgets-data-cell"}
                 (merge (when col-span) {:colSpan col-span}))
         cell]))))

;; ---------------------------------------------------------------------
;; Row Builder Multimethod
;;
;; NOTE: we just implement the default method, if you want a custom
;; row, you just need to require row-builder ([om-widgets.grid :refer [row-builder]])
;; and extend the multimethod with a valid om component.

(defmulti row-builder (fn [row owner opts] (:row-type row)))

(defmethod row-builder :default
  [row owner opts]
  (reify
    om/IDisplayName
    (display-name [_] "DefaultRow")

    om/IRenderState
    (render-state [this state]
      (apply dom/tr #js {:className (str (when (:target state)
                                           "om-widgets-default-row")
                                         (when (= row (:target state))
                                           (str " " (or (and (:selected-row-style opts)
                                                             (name (:selected-row-style opts)))
                                                        "active"))))
                         :onMouseDown #(let [props (om/get-props owner)]
                                        (put! (:channel state)
                                              {:row (if (satisfies? IDeref props)
                                                      @props
                                                      props)}))}
             (map (fn [{:keys [field] :as column}]
                    (om/build cell-builder (field row) {:opts {:column-def column :row row}}))
                  (:columns opts))))))

;; ---------------------------------------------------------------------
;; Grid Pager Multimethod
;;
;; NOTE: we just implement the default method, and the :none method, if you want a custom
;; pager, you just need to require grid-pager ([om-widgets.grid :refer [grid-pager]])
;; and provide a custom implementation

(defmulti grid-pager (fn [pager-definition _] (:type pager-definition)))

(defmethod grid-pager :default
  [pager-definition state options]
  (om/build default-pager pager-definition {:state state :opts options}))

(defmethod grid-pager :none [_ _ _])


(defn- grid-body
  [target owner opts]
  (reify
    om/IDisplayName
    (display-name [_] "GridBody")

    om/IRenderState
    (render-state [this {:keys [rows] :as state}]
      (dom/table #js {:className (str "table data"
                                      (when (:hover? opts) " table-hover")
                                      (when (:condensed? opts) " table-condensed")
                                      (when (:bordered? opts) " table-bordered")
                                      (when (:striped? opts) " table-striped"))}
                 (om/build header (:columns opts)
                           {:state {:channel (:channel state)
                                    :sort-info (:sort-info state)}})
                 (apply dom/tbody #js {}
                        (om/build-all row-builder rows {:state {:channel (:channel state)
                                                                :target target}
                                                        :opts {:columns (:columns opts)
                                                               :selected-row-style (:selected-row-style opts)}}))))))

;; This function where private but we cannot test it from outside given the lack of #' reader
;; https://github.com/clojure/clojurescript/wiki/Differences-from-Clojure#the-reader
(defn data-page [source current-page page-size events-channel sort-info]
  (let [sorter (when sort-info
                 (grid-sorter (:column sort-info)))
        rows (vec (if (and sorter
                           (satisfies? ISortableColumnSortData sorter))
                    (sort-data sorter sort-info (:rows source))
                    (:rows source)))
        top (count rows)
        start (* current-page page-size)
        end (+ start page-size)
        total-rows (:total-rows source)
        end-gap (min page-size (if (> (- end (+ top (:index source))) 0)
                                 (- end (+ top (:index source)))
                                 0))
        begin-gap (min page-size (max 0 (- (:index source) start)))
        drop-cut (max 0 (min top (+ (- start (:index source)) begin-gap)))
        take-cut (max 0 (min top (+ (- end (:index source)) end-gap)))]

    (when (and events-channel
               (or (< (max 0 (- start (* 1 page-size))) (:index source))
                   (> (min total-rows (+ (+ start page-size) (* 2 page-size))) (+ (:index source) top))))
      (go
        (>! events-channel {:sort-info sort-info
                            :event-type :request-range
                            :start (min total-rows (max 0 (- start (* 4 page-size))))
                            :end (min total-rows (+ (+ start page-size) (* 5 page-size)))})))
    (->> rows
         (take take-cut)
         (drop drop-cut)
         (concat))))

(defn- create-grid [target owner opts]
  (reify
    om/IDisplayName
    (display-name [_] "Grid")

    om/IInitState
    (init-state [_]
      {:channel (chan)})

    om/IWillMount
    (will-mount [_]
      (go-loop []
               (let [msg (<! (om/get-state owner :channel))]
                 (when-not (= msg :quit)
                   (cond
                     (and target
                          (:row msg)) (om/update! target (:row msg))
                     (:sort-info msg) (om/set-state! owner :sort-info (:sort-info msg))
                     (:new-page msg) (om/set-state! owner :current-page (:new-page msg)))
                   (recur)))))

    om/IRenderState
    (render-state [this {:keys [header src] :as state}]
      (dom/div #js {:className "om-widgets-grid"
                    :id (:id opts)}
               (om/build grid-body
                         target
                         {:state {:rows (data-page src
                                                   (:current-page state)
                                                   (:page-size state)
                                                   (:events-channel state)
                                                   (:sort-info state))
                                  :channel (:channel state)
                                  :sort-info (:sort-info state)}
                          :opts {:columns (:columns header)
                                 :hover? (:hover? opts)
                                 :condensed? (:condensed? opts)
                                 :bordered? (:bordered? opts)
                                 :striped? (:striped? opts)
                                 :selected-row-style (:selected-row-style opts)}})
               (grid-pager (:pager state) {:total-rows (:total-rows src)
                                           :channel (:channel state)
                                           :page-size (:page-size state)
                                           :current-page (:current-page state)
                                           :max-pages (:max-pages state)} opts)))))

;; This function where private but we cannot test it from outside given the lack of #' reader
;; https://github.com/clojure/clojurescript/wiki/Differences-from-Clojure#the-reader
(defn calculate-max-pages
  [total-rows page-size]
  (- (int (/ total-rows page-size))
     (if (= 0 (mod total-rows page-size)) 1 0)))

;; ---------------------------------------------------------------------
;; Schema

(def HeaderSchema
  {:type (s/enum :default :none)
   (s/optional-key :columns) [{:caption s/Str
                               :field s/Keyword
                               :sort (s/either s/Bool s/Keyword)
                               (s/optional-key :col-span) s/Int
                               :data-format (s/enum :default :date :dom :keyword)
                               ;; for date format
                               :date-formatter s/Str
                               ;; for dom format
                               (s/optional-key :fn) (s/pred fn?)
                               ;; for keywords
                               (s/optional-key :options) (s/pred coll?)}]})

(def GridSourceSchema
  {:rows [{s/Keyword s/Any}]
   (s/optional-key :index) s/Num
   (s/optional-key :total-rows) s/Num})

(def GridSchema
  {(s/optional-key :id) s/Str
   (s/optional-key :hover?) s/Bool
   (s/optional-key :condensed?) s/Bool
   (s/optional-key :bordered?) s/Bool
   (s/optional-key :striped?) s/Bool
   (s/optional-key :selected-row-style) (s/enum :active :success :info :warning :danger)
   (s/optional-key :onChange) (s/pred fn?)
   (s/optional-key :events-channel) s/Any
   (s/optional-key :header) HeaderSchema
   (s/optional-key :pager) (s/enum :default :none)
   (s/optional-key :language) (s/enum :en :es)})

;; ---------------------------------------------------------------------
;; Public

;; TODO source should be a DataSource protocol
(defn grid [source target & {:keys [id onChange events-channel header pager language]
                             :as definition}]
  (let [src {:rows (or (:rows source) source)
             :index (or (:index source) 0)
             :total-rows (or (:total-rows source) (count source))}
        page-size (or (:page-size definition) 5)]
    (om/build create-grid
              target
              {:init-state {:current-page (int (/ (:index src) page-size))}
               :state {:src src
                       :header (or header {:type :default})
                       :pager (or pager {:type :default})
                       :events-channel events-channel
                       :max-pages (calculate-max-pages (:total-rows src) page-size)
                       :page-size page-size
                       :onChange onChange}
               :opts {:language (or (:language definition) :en)
                      :hover? (:hover? definition)
                      :condensed? (:condensed? definition)
                      :bordered? (:bordered? definition)
                      :striped? (:striped? definition)
                      :selected-row-style (:selected-row-style definition)
                      :id id}})))
