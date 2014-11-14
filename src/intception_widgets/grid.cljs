(ns intception-widgets.grid
  (:require-macros [cljs.core.async.macros :refer [go]])

  (:require [om.core :as om :include-macros true]
     [cljs.core.async :refer [put! chan <! alts! timeout]]
     [intception-widgets.translations :as translations]
     [om.dom :as dom :include-macros true]))

(defn- title-header-cell [{:keys [caption]}]
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

(defn- default-header [header-definition owner opts]
  (reify
    om/IRenderState
    (render-state [this state]
       (dom/table #js{:className "table header"}
            (om/build header (:columns header-definition))))))

(defn- default-pager [pager-definition owner opts]
  (reify
    om/IRenderState
      (render-state [this state]
        (let [current-page (om/get-state owner :current-page)
              max-pages (om/get-state owner :max-pages)
              total-rows (:total-rows state)
              language (:language opts)]
          (dom/ul #js {:className "pager"}
             ;; previous page
            (dom/li #js {:className (when (= 0 current-page)
                                      "disabled")}
              (dom/a #js {
                          :onClick #(when (> current-page 0)
                                                (om/set-state! owner :current-page (dec current-page))
                                                false)} (translations/translate :grid
                                                                                language
                                                                                :default-pager/previous-page))
            ;; next page
            (dom/li #js {:className (when (= current-page max-pages)
                                      "disabled")}
              (dom/a #js { :onClick #(when (< current-page max-pages)
                                                (om/set-state! owner :current-page (inc current-page))
                                                false)} (translations/translate :grid
                                                                                language
                                                                                :default-pager/next-page)))
            (dom/span #js {:className "totals"}
                        (format (translations/translate :grid
                                                language
                                                :default-pager/total-rows) total-rows))))))))

(defmulti grid-header (fn [header-definition _] (:type header-definition)))

(defmethod grid-header :default
  [header-definition state-and-options]
    (om/build default-header header-definition state-and-options))

(defmethod grid-header :none [_ _ _])

(defmulti row-builder (fn [row-data _ _] (:row-type row-data)))

(defmethod row-builder :default
  [row-data _ _]
  (reify
    om/IDisplayName
      (display-name[_] "DefaultRow")
    om/IRenderState
    (render-state [this state]
      (apply dom/tr #js{:className (:class row-data)
                        :onMouseDown (fn [e]
                                       (om/update! (:target (:parent-state state)) (:row row-data))
                                       (om/refresh! (:parent state)))}
             (om/build-all data-cell (vals (:projection row-data)))))))

(defmulti grid-pager (fn [pager-definition _] (:type pager-definition)))

(defmethod grid-pager :default
  [pager-definition state-and-options]
  (om/build default-pager pager-definition state-and-options))

(defmethod grid-pager :none [_ _])

(defn- build-data [columns rows selected-row]
  (let [fields (map #(:field %) columns)]
   (->>
      rows
      (map #(assoc {} :row % :row-type (:row-type %)))
      (map #(assoc % :projection (select-keys (:row %) fields)))
      (map #(assoc % :class (if (= (select-keys selected-row fields) (:projection %)) "success" ""))))))

(defn- grid-data [data owner opts]
  (reify
    om/IRenderState
    (render-state [this state]
      (let [rows (build-data (:columns (:header (om/get-state (:parent data))))
                             (:rows data)
                             (:target data))]
        (dom/table #js{:className "table data"}
          (apply dom/tbody #js{}
            (om/build-all row-builder rows {:state {:parent owner :parent-state data} :opts opts})))))))

(defn- data-page [source current-page page-size events-channel fixed-height]
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

    (when (and events-channel (or (< (max 0 (- start (* 1 page-size))) index)
                                  (> (min total-rows (+ (+ start page-size) (* 2 page-size))) (+ index top))))
      (go
        (>! events-channel {:event-type :request-range
                            :start (min total-rows (max 0 (- start (* 4 page-size))))
                            :end (min total-rows (+ (+ start page-size) (* 5 page-size)))})))

    (concat (repeat (min page-size begin-gap) -1)
            (subvec rows (max 0 (min top (+ (- start  (:index source)) begin-gap))) (max 0 (min top (+ (- end  (:index source)) end-gap))))
            (when (nil? fixed-height)
                (repeat (min page-size end-gap) -1)))))

(defn- create-grid [source owner opts]
  (reify
    om/IRenderState
    (render-state [this state]
        (let [ path (:path state)
               st (->> {}
                    (merge (when (:height state) {:height (:height state)})))]
          (dom/div #js {:className "grid"}
            (grid-header (:header state) {:opts opts})
            (dom/div #js {:className "scrollable" :style (clj->js st)}
              (om/build grid-data
                        {:parent owner
                         :rows (data-page source
                                 (:current-page state)
                                 (:page-size state)
                                 (:events-channel state)
                                 (:height state))
                         :target (:target state)}
                         {:opts opts}))
            (grid-pager (:pager state) {:state {:total-rows (:total-rows source)} :opts opts}))))))

(defn grid [source target & {:keys [id onChange events-channel height header pager] :as definition}]
  (let [src (if (or (seq? source) (vector? source))
                {:index 0 :rows source :total-rows (count source)}

                {:index (or (:index source) 0 )
                 :total-rows (or (:total-rows source) (count (:rows source)))
                 :rows (vec (:rows source))})
         page-size (or (:page-size definition) 5)]
    (om/build create-grid
              src
              {:init-state {
                :current-page (int (/ (:index src) page-size))}

               :state {
                :target target
                :header (or header {:type :default})
                :pager (or pager {:type :default})
                :id id
                :events-channel events-channel
                :max-pages (- (int (/ (:total-rows src) page-size)) (if (= 0 (mod (:total-rows src) page-size)) 1 0))
                :page-size page-size
                :onChange onChange
                :height height}
               :opts {
                :language (or (:language definition) :en)}})))

