(ns om-widgets.grid
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [om-widgets.utils :as utils]
            [om-widgets.checkbox :refer [checkbox]]
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

(defprotocol ISortableColumnDefaultSortData
  (default-sort-data [_ column]))

(defn standard-sort-caret
  [_ owner]
  (om/component
    (let [sort-info (om/get-state owner :sort-info)
          column (om/get-state owner :column)]
      (html [:div {:class "om-widgets-sortable-column"
                   :onClick (fn [e]
                              (let [sort-info (om/get-state owner :sort-info)
                                    column (if (satisfies? IDeref (om/get-state owner :column))
                                             @(om/get-state owner :column)
                                             (om/get-state owner :column))]
                                (put! (om/get-state owner :channel)
                                      {:type :sort
                                       :sort-info {:column column
                                                   :direction (if (and sort-info
                                                                       (= (:column sort-info)
                                                                          column)
                                                                       (= (:direction sort-info)
                                                                          :up))
                                                                :down
                                                                :up)}})
                                (.preventDefault e)))}
             (:caption column)
             [:span {:class (str "pull-right glyphicon om-widgets-sortable-"
                                 (if (and sort-info
                                          (= (select-keys (:column sort-info) [:field :caption])
                                             (select-keys column [:field :caption])))
                                   (name (:direction sort-info))
                                   "both"))}]]))))

(defn standard-sort
  [column]
  (reify
    ISortableColumnDefaultSortData
    (default-sort-data [this column]
      {:column column
       :direction :up})

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

(defn- title-header-cell
  [{:keys [caption col-span] :as h} owner]
  (om/component
    (let [sorter (grid-sorter h)]
      (html [:th (-> {}
                     (when col-span (merge {:colSpan col-span})))
             (if (and sorter
                      (satisfies? ISortableColumnCaret sorter))
               (column-caret sorter
                             h
                             (om/get-state owner :sort-info)
                             (om/get-state owner :channel))
               caption)]))))


(defn- header
  [columns owner]
  (reify
    om/IRenderState
    (render-state [this {:keys [channel sort-info all-selected?] :as state}]
      (html
        [:thead {:class "om-widgets-title-header-row"}
         (utils/make-childs [:tr
                             (when (om/get-state owner :multiselect?)
                               [:th {:class "col-md-1 col-sm-1 col-lg-1 col-xs-1"}
                                [:input {:type "checkbox"
                                         :id "select-all"
                                         :checked all-selected?
                                         :onChange (fn [e]
                                                     (do (put! (om/get-state owner :selection-channel)
                                                               {:type :select-all
                                                                :checked? (.. e -target -checked)})
                                                         (.preventDefault e)))}]])]

                            (om/build-all title-header-cell columns {:state {:channel channel
                                                                             :sort-info sort-info}}))]))))

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
        (html
          [:nav
           [:span {:class "current-page pull-right"}
            (u/format (translate language :grid.pager/total-rows)
                      (:start page-info)
                      (:end page-info)
                      total-rows)]

           [:ul {:class "pager"}
            [:li {:class (when previous-disabled? "disabled")}
             [:a {:onClick #(when (> current-page 0)
                              (put! (:channel state) {:type :change-page
                                                      :new-page (dec current-page)})
                              (.preventDefault %))}
              (translate language :grid.pager/previous-page)]]

            [:li {:class (when next-disabled? "disabled")}
             [:a {:onClick #(when (< current-page max-pages)
                              (put! (:channel state) {:type :change-page
                                                      :new-page (inc current-page)})
                              (.preventDefault %))}
              (translate language :grid.pager/next-page)]]]])))))


(defn- text-alignment
  [opts]
  (when-let [text-alignment (:text-alignment (:column-def opts))]
    (str "text-" (name text-alignment))))

(defmulti cell-builder (fn [cell owner opts]
                         (:data-format (:column-def opts))))

(defmethod cell-builder :date
  [date owner opts]
  (let [col-span (:col-span (:column-def opts))
        date-formatter (:date-formatter (:column-def opts))]
    (om/component
      (html
        [:td (-> {:class ["om-widgets-data-cell" (text-alignment opts)]}
                 (merge (when col-span) {:colSpan col-span}))
         (if date
           (timef/unparse (timef/formatter (or date-formatter
                                               "yyyy/MM/dd"))
                          (time/date-time date))
           "")]))))

(defmethod cell-builder :keyword
  [cell owner opts]
  (let [col-span (:col-span (:column-def opts))
        options (:options (:column-def opts))]
    (om/component
      (html
        [:td (-> {:class ["om-widgets-data-cell" (text-alignment opts)]}
                 (merge (when col-span) {:colSpan col-span}))
         (or (get options cell)
             cell)]))))

(defmethod cell-builder :dom
  [cell owner opts]
  (let [col-span (:col-span (:column-def opts))
        content (:fn (:column-def opts))]
    (om/component
      (html
        [:td (-> {:class ["om-widgets-data-cell" (text-alignment opts)]}
                 (merge (when col-span) {:colSpan col-span}))
         (content cell (:row opts))]))))

(defmethod cell-builder :default
  [cell owner opts]
  (let [col-span (:col-span (:column-def opts))]
    (om/component
      (html
        [:td (-> {:class ["om-widgets-data-cell" (text-alignment opts)]}
                 (merge (when col-span) {:colSpan col-span}))
         cell]))))

;; ---------------------------------------------------------------------
;; Row Builder Multimethod
;;
;; NOTE: we just implement the default method, if you want a custom
;; row, you just need to require row-builder ([om-widgets.grid :refer [row-builder]])
;; and extend the multimethod with a valid om component.

(defn row-class
  [row target opts]
  (let [style-class (or (and (:selected-row-style opts)
                             (name (:selected-row-style opts)))
                        "active")]
    [(when (and target
                (not (:disable-selection? opts)))
       "om-widgets-default-row")

     (if (:multiselect? opts)
       (when (contains? target row) style-class)
       (when (= row target) style-class))]))

(defmulti row-builder (fn [row owner opts] (:row-type row)))

(defmethod row-builder :default
  [row owner opts]
  (reify
    om/IRenderState
    (render-state [this state]
      (html
        (utils/make-childs
          [:tr (-> {:class (row-class row (:target state) opts)}
                   (merge (when-not (:disable-selection? opts)
                            ;; we use mousedown because onClick its triggered before,
                            ;; this make links inside cells work as expected
                            {:onMouseDown #(let [props (om/get-props owner)]
                                             (put! (om/get-state owner :channel)
                                                   {:type :select
                                                    :row (if (satisfies? IDeref props) @props props)})
                                             (.preventDefault %))})))

           (when (:multiselect? opts)
             [:td
              [:input {:type "checkbox"
                       :id (str "multiselect-" (:index state))
                       :checked (contains? (:target state) row)
                       :onMouseDown (fn [e] (.stopPropagation e))
                       :onChange (fn [e]
                                   (let [props (om/get-props owner)
                                         channel (om/get-state owner :channel)]
                                     (when channel
                                       (put! channel
                                             {:type :multiselect
                                              :checked? (.. e -target -checked)
                                              :row (if (satisfies? IDeref props) @props props)}))
                                     (.preventDefault e)))}]])]

          (map (fn [{:keys [field] :as column}]
                 (om/build cell-builder (field row) {:opts {:column-def column :row row}}))
               (:columns state)))))))

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
    om/IInitState
    (init-state [_]
      {:selection-channel (chan)})

    om/IWillMount
    (will-mount [_]
      (go-loop
        []
        (let [events-chan (om/get-state owner :events-channel)
              msg (<! (om/get-state owner :selection-channel))]
          (when-not (= msg :quit)
            (condp = (:type msg)
              :select
              (do
                (when-not (:multiselect? opts)
                  (om/update! (om/get-props owner) (:row msg)))

                (when events-chan
                  (put! events-chan
                        {:event-type :row-selected
                         :row (:row msg)})))

              :multiselect
              (do
                (om/transact! (om/get-props owner)
                              (fn [s]
                                (if (:checked? msg)
                                  (conj s (:row msg))
                                  (disj s (:row msg)))))

                (when events-chan
                  (put! events-chan
                        {:event-type :multiselect-row
                         :row (:row msg)}))

                (when (not (:checked? msg))
                  (om/set-state! owner :all-selected? false)))

              :select-all
              (let [rows (om/get-state owner :rows)]
                (om/update! (om/get-props owner) (if (:checked? msg) (into #{} rows) #{}))
                (when events-chan
                  (put! events-chan
                        {:event-type :multiselect-all
                         :rows rows
                         :all-selected? (:checked? msg)}))
                (om/set-state! owner :all-selected? (:checked? msg))))
            (recur)))))

    om/IWillUnmount
    (will-unmount [this]
      (go
        (put! (om/get-state owner :channel) :quit)))

    om/IRenderState
    (render-state [this {:keys [rows] :as state}]
      (html
        [:table {:class ["table data"
                         (when (:hover? opts) "table-hover")
                         (when (:condensed? opts) "table-condensed")
                         (when (:bordered? opts) "table-bordered")
                         (when (:striped? opts) "table-striped")]}
         (om/build header (:columns state)
                   {:state {:channel (:channel state)
                            :selection-channel (:selection-channel state)
                            :all-selected? (:all-selected? state)
                            :sort-info (:sort-info state)
                            :multiselect? (:multiselect? opts)}})

         (utils/make-childs [:tbody]
                            (map-indexed #(om/build row-builder %2 (merge {:state {:channel (:selection-channel state)
                                                                                   :events-channel (:events-channel state)
                                                                                   :target target
                                                                                   :index %1
                                                                                   :columns (:columns state)}
                                                                           :opts {:multiselect? (:multiselect? opts)
                                                                                  :disable-selection? (:disable-selection? opts)
                                                                                  :selected-row-style (:selected-row-style opts)}}
                                                                          (when (:key-field state)
                                                                            {:react-key (get %2 (:key-field state))})))
                                         rows))]))))

;; This function where private but we cannot test it from outside given the lack of #' reader in clojurescript
;; https://github.com/clojure/clojurescript/wiki/Differences-from-Clojure#the-reader
(defn data-page
  [source current-page page-size events-channel sort-info force-page-reload]
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
               (or @force-page-reload
                   (< (max 0 (- start (* 1 page-size))) (:index source))
                   (> (min total-rows (+ (+ start page-size) (* 2 page-size))) (+ (:index source) top))))
      (go
        (>! events-channel {:sort-info sort-info
                            :event-type :request-range
                            :start (min total-rows (max 0 (- start (* 4 page-size))))
                            :end (min total-rows (+ (+ start page-size) (* 5 page-size)))})
        (reset! force-page-reload false)))
    (->> rows
         (take take-cut)
         (drop drop-cut)
         (concat))))

(defn- create-grid
  [target owner opts]
  (reify
    om/IInitState
    (init-state [_]
      {:channel (chan)
       :force-page-reload (atom false)})

    om/IWillMount
    (will-mount [_]
      (go-loop
        []
        (let [msg (<! (om/get-state owner :channel))]
          (when-not (= msg :quit)
            (condp = (:type msg)
              :sort
              (do
                (reset! (om/get-state owner :force-page-reload) true)
                (om/set-state! owner :sort-info (:sort-info msg)))

              :change-page
              (do
                (om/set-state! owner :current-page (:new-page msg))
                (when-let [events-chan (om/get-state owner :events-channel)]
                  (put! events-chan
                        {:event-type :page-changed
                         :new-page (:new-page msg)}))))

            (recur)))))

    om/IRenderState
    (render-state [this {:keys [header src] :as state}]
      (html
        [:div (-> {:class ["om-widgets-grid" (when (:responsive? opts) "table-responsive")]}
                  (merge (when (:id opts)) {:id (:id opts)}))

         (om/build grid-body
                   target
                   {:state {:rows (data-page src
                                             (:current-page state)
                                             (:page-size state)
                                             (:events-channel state)
                                             (:sort-info state)
                                             (:force-page-reload state))
                            :events-channel (:events-channel state)
                            :key-field (:key-field state)
                            :channel (:channel state)
                            :sort-info (:sort-info state)
                            :columns (:columns header)}
                    :opts {:hover? (:hover? opts)
                           :condensed? (:condensed? opts)
                           :bordered? (:bordered? opts)
                           :striped? (:striped? opts)
                           :multiselect? (:multiselect? opts)
                           :disable-selection? (:disable-selection? opts)
                           :selected-row-style (:selected-row-style opts)}})

         (grid-pager (:pager state) {:total-rows (:total-rows src)
                                     :channel (:channel state)
                                     :page-size (:page-size state)
                                     :current-page (:current-page state)
                                     :max-pages (:max-pages state)} opts)]))))

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
   :columns [{:caption s/Str
              :field s/Keyword
              :sort (s/either s/Bool s/Keyword)
              (s/optional-key :col-span) s/Int
              (s/optional-key :text-alignment) (s/enum :left :center :right :justify :nowrap)
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
  {:header HeaderSchema
   (s/optional-key :id) s/Str
   (s/optional-key :hover?) s/Bool
   (s/optional-key :condensed?) s/Bool
   (s/optional-key :bordered?) s/Bool
   (s/optional-key :striped?) s/Bool
   (s/optional-key :multiselect?) s/Any
   (s/optional-key :selected-row-style) (s/enum :active :success :info :warning :danger)
   (s/optional-key :onChange) (s/pred fn?)
   (s/optional-key :events-channel) s/Any
   (s/optional-key :page-size) s/Int
   (s/optional-key :current-page) s/Int
   (s/optional-key :pager) {:type (s/enum :default :none)}
   (s/optional-key :language) (s/enum :en :es)})

;; ---------------------------------------------------------------------
;; Public

;; TODO source should be a DataSource protocol?
(defn grid
  [source target & {:keys [id onChange events-channel key-field header pager language disable-selection? responsive?] :as definition}]
  (let [src {:rows (or (:rows source) source)
             :index (or (:index source) 0)
             :total-rows (or (:total-rows source) (count source))}
        init-sorted-column (first (filter #(= (:field %)
                                              (get-in header [:start-sorted :by]))
                                          (:columns header)))
        sorter (when init-sorted-column
                 (grid-sorter init-sorted-column))
        page-size (or (:page-size definition) 5)
        current-page (or (:current-page definition))]
    (om/build create-grid
              target
              {:init-state {:current-page current-page
                            :sort-info (when (and sorter
                                                  (satisfies? ISortableColumnDefaultSortData sorter))
                                         (merge (default-sort-data sorter init-sorted-column)
                                                (get header :start-sorted)))}
               :state (-> {:src src
                           :header header
                           :pager (or pager {:type :default})
                           :events-channel events-channel
                           :key-field key-field
                           :max-pages (calculate-max-pages (:total-rows src) page-size)
                           :page-size page-size
                           :onChange onChange}
                          (merge (when  (:current-page definition)
                                          {:current-page current-page})))
               :opts {:language (or language :en)
                      :hover? (:hover? definition)
                      :condensed? (:condensed? definition)
                      :bordered? (:bordered? definition)
                      :striped? (:striped? definition)
                      :multiselect? (:multiselect? definition)
                      :responsive? responsive?
                      :disable-selection? disable-selection?
                      :selected-row-style (:selected-row-style definition)
                      :id id}})))
