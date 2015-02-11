(ns om-widgets.grid
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [schema.core :as s :include-macros true]
            [cljs.core.async :refer [put! chan <! alts! timeout close!]]
            [om-widgets.translations :refer [translate]]
            [om-widgets.utils :as u]))


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


(defn- title-header-cell [{:keys [caption]}]
  (om/component
   (dom/th #js {} caption)))

(defn- header [columns]
  (om/component
   (dom/thead #js {:className "om-widgets-title-header-row"}
              (apply dom/tr nil
                     (om/build-all title-header-cell columns)))))

(defn- data-cell [text]
  (om/component
   (dom/td #js {:className "om-widgets-data-cell"} text)))

(defn- default-header [header-definition owner opts]
  (reify
    om/IRenderState
    (render-state [this state]
      (dom/table #js {:className "om-widgets-table om-widgets-header"}
                 (om/build header (:columns header-definition))))))

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
    (render-state [this {:keys [current-page max-pages total-rows page-size current-page-total] :as state}]
      (let [page-info (build-page-boundaries {:current-page (inc current-page)
                                              :page-size page-size
                                              :total-items total-rows})]
        (dom/ul #js {:className "pager"}
                ;; previous page
                (dom/li #js {:className (when (= 0 current-page) "disabled")}
                        (dom/a #js {:onClick #(when (> current-page 0)
                                                (put! (:channel state) {:new-page (dec current-page)})
                                                false)}
                               (translate language :grid.pager/previous-page)))

                ;; next page
                (dom/li #js {:className (when (= current-page max-pages) "disabled")}
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

(defn- build-row-data [columns row selected-row]
  (let [fields (map #(:field %) columns)]
    (merge {} {:projection (select-keys row fields)
               :class (str "om-widgets-default-row"
                           (when (= selected-row row)
                             " success"))})))


;; ---------------------------------------------------------------------
;; Grid Header Multimethod
;;
;; NOTE: we just implement the default method, and the :none method, if you want a custom
;; header, you just need to require grid-header ([om-widgets.grid :refer [grid-header]])
;; and provide a custom implementation

(defmulti grid-header (fn [header-definition _ _] (:type header-definition)))

(defmethod grid-header :default
  [header-definition state options]
  (om/build default-header header-definition {:state state :opts options}))

(defmethod grid-header :none [_ _ _])


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
      (let [row-data (build-row-data (:columns opts) row (:target opts))]
        (apply dom/tr #js {:className (:class row-data)
                           :onMouseDown #(put! (:channel state) {:row (if (satisfies? IDeref row)
                                                                        @row
                                                                        row)})}
               (om/build-all data-cell (vals (:projection row-data))))))))

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

    om/IWillMount
    (will-mount [_]
      (go-loop []
        (let [msg (<! (om/get-state owner :channel))]
          (when-not (= msg :quit)
            (om/update! target (:row msg))
            (recur)))))
    om/IInitState
    (init-state [_]
      {:channel (chan)})

    om/IWillUnmount
    (will-unmount [_]
      (put! (om/get-state owner :channel) :quit))

    om/IRenderState
    (render-state [this {:keys [rows] :as state}]
      (dom/table #js {:className "table data"}
                 (om/build header (:columns opts))
                 (apply dom/tbody #js {}
                        (om/build-all row-builder rows {:state {:channel (:channel state)}
                                                        :opts {:columns (:columns opts)
                                                               :target target}}))))))

;; This function where private but we cannot test it from outside given the lack of #' reader
;; https://github.com/clojure/clojurescript/wiki/Differences-from-Clojure#the-reader
(defn data-page [source current-page page-size events-channel]
  (let [rows (vec (:rows source))
        top (count rows)
        start (* current-page page-size)
        end  (+ start page-size)
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
        (>! events-channel {:event-type :request-range
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
            (om/set-state! owner :current-page (:new-page msg))
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
                                                   (:events-channel state))}
                          :opts {:columns (:columns header)}})
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
                               :field s/Keyword}]})

(def GridSourceSchema
  {:rows [{s/Keyword s/Any}]
   (s/optional-key :index) s/Num
   (s/optional-key :total-rows) s/Num})

(def GridSchema
  {(s/optional-key :id) s/Str
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
                      :id id}})))

