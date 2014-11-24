(ns intception-widgets.grid
  (:require-macros [cljs.core.async.macros :refer [go]])

  (:require [om.core :as om :include-macros true]
     [cljs.core.async :refer [put! chan <! alts! timeout]]
     [intception-widgets.utils :as utils]
     [om.dom :as dom :include-macros true]))

(defn- title-header-cell [{:keys [caption :caption]}]
  (om/component
    (dom/th #js {} caption)))

(defn- header [columns]
  (om/component
    (dom/thead #js{:className "title-header-row"}
      (apply  dom/tr nil
        (om/build-all title-header-cell columns)))))

(defn- data-cell [text]
  (om/component
      (dom/td #js {:className "data-cell"} text)))

(defn- dummy-cell []
  (om/component
      (dom/td #js {:className "dummy-col"} "!&nbsp;")))

(defn- data-row [row-data]
    (reify
      om/IRenderState
      (render-state [this state]
          (if (not= (:row (:rec row-data)) -1 )
            (apply dom/tr #js{ :className (:class row-data)
                             :onMouseDown (fn [e]
                                (utils/om-update! (:target (:parent-state state)) (:key (:parent-state state)) (:row (:rec row-data)))
                                (om/refresh! (:parent state))
                                )}
                  (om/build-all data-cell (:text-data (:rec row-data))))
            (apply dom/tr nil
                   (om/build-all dummy-cell (repeat  (count (:columns (:parent-state state))) [])))))))

(defn build-data [columns convertions rows]
   (map (fn [row] {:text-data (map #((get convertions (:field %)) (get row (:field %))) columns) :row row }) rows))

(defn- grid-header [columns owner]
    (reify
      om/IRenderState
      (render-state [this state]
         (dom/table #js{:className "table header"}
              (om/build header columns)))))

(defn- grid-data [data owner]
    (reify
      om/IRenderState
      (render-state [this state]
        (let [c (if (:key data) (utils/om-get (:target data) (:key data)) (:target data))
              current (:text-data (first (build-data (:columns data) (:convertions data) [c])))
              fields (map (fn [rec class]
                             {:rec rec :class (str "" (when (= (:text-data rec) current) "success"))})
                          (build-data (:columns data) (:convertions data) (:rows data)))]

            (dom/table #js{:className "table data"}
              (om/build header (:columns data))
              (apply dom/tbody #js{}
                (om/build-all data-row fields { :state {:parent owner :parent-state data}})))))))

(defn- grid-pager [parent owner]
  (reify
    om/IRenderState
      (render-state [this state]
        (let [current-page (om/get-state parent :current-page)
              max-pages (om/get-state parent :max-pages)
              total-rows (:total-rows state)]
          (dom/ul #js {:className "pager"}

             ;; previous page
            (dom/li #js {:className (when (= 0 current-page)
                                      "disabled")}
              (dom/a #js {
                          :onClick #(when (> current-page 0)
                                                (om/set-state! parent :current-page (dec current-page))
                                                false)} "« Previa"))
            ;; next page
            (dom/li #js {:className (when (= current-page max-pages)
                                      "disabled")}
              (dom/a #js { :onClick #(when (< current-page max-pages)
                                                (om/set-state! parent :current-page (inc current-page))
                                                false)} "Siguiente »"))
            (dom/span #js {:className "totals"} (str total-rows " en total") ))))))

(defn- data-page [source current-page page-size c fixed-height]
  (let [rows (vec (:rows source ))
        top (count rows)
        start (* current-page page-size)
        end  (+ start page-size)
        total-rows (:total-rows source)
        index (:index source)
        end-gap (min page-size (if (> (- end (+ top (:index source))) 0)
                                   (- end (+ top (:index source)))
                                   0))
        begin-gap (min page-size (max 0 (- (:index source) start)))]


    (when (and c (or (< (max 0 (- start (* 1 page-size))) index)
                     (> (min total-rows (+ (+ start page-size) (* 2 page-size))) (+ index top))))
      (go
        (>! c {:event-type :request-range
               :start (min total-rows (max 0 (- start (* 4 page-size))))
               :end (min total-rows (+ (+ start page-size) (* 5 page-size)))})))

    (concat (repeat (min page-size begin-gap) -1)
            (subvec rows (max 0 (min top (+ (- start  (:index source)) begin-gap))) (max 0 (min top (+ (- end  (:index source)) end-gap))))
            (when (nil? fixed-height)
                (repeat (min page-size end-gap) -1)))))


(defn- create-grid [source owner]
  (reify
    om/IRenderState
    (render-state [this state]
        (let [ path (:path state)
               st (->> {}
                    (merge (when (:height state) {:height (:height state)})))]
          (dom/div #js {:className "om-widgets-grid"}

            (om/build grid-header (:columns state))
            (dom/div #js {:className "scrollable" :style (clj->js st)}
              (om/build grid-data  {:parent owner
                                    :columns (:columns state)
                                    :rows (data-page source
                                                     (:current-page state)
                                                     (:page-size state)
                                                     (:events-channel state)
                                                     (:height state))
                                    :key (:key state)
                                    :target (:target state)
                                    :convertions (:convertions state)}))
            (om/build grid-pager  owner {:state { :total-rows (:total-rows source) }}))))))

(defn- build-convertions [columns]
  (apply hash-map
         (interleave
          (map (fn [col]
                  (:field col)) columns)
          (map (fn [col]
                  (cond
                     ;;                     (= (:input-format col) "date" ) #(if % (utils/get-utc-formatted-date %) "")
                     :else #(if % (str %) ""))) columns))))

(defn grid [source target {:keys [id onChange  columns page-size events-channel height key] :or {page-size 5}} ]
  (let [src (if (or (seq? source) (vector? source))
                {:index 0 :rows source :total-rows (count source)}

                {:index (or (:index source) 0 )
                 :total-rows (or (:total-rows source) (count (:rows source)))
                 :rows (vec (:rows source))})]
    (om/build create-grid src
              {:init-state {
                :current-page (int (/ (:index src) page-size))
              }
              :state {
                      :target target
                      :id id
                      :key key
                      :events-channel events-channel
                      :max-pages (- (int (/ (:total-rows src) page-size)) (if (= 0 (mod (:total-rows src) page-size)) 1 0))
                      :page-size page-size
                      :onChange onChange
                      :columns columns
                      :height height
                      :convertions (build-convertions columns)}})))
