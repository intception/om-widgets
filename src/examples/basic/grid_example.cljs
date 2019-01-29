(ns examples.basic.grid-example
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]
                   [pallet.thread-expr :as th])
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [om-widgets.grid :refer [row-builder]]
            [cljs.core.async :refer [put! chan <! alts! timeout]]
            [sablono.core :as html :refer-macros [html]]
            [om-widgets.core :as w]))


(defn- grid-simple
  [cursor owner]
  (reify
    om/IRenderState
    (render-state [_ state]
      (html
        [:div.panel.panel-default
         [:div.panel-heading (str "Grid (selected cursor value: "
                                  (get-in cursor [:selected :name]) " )")]
         [:div.panel-body
          (w/grid (get-in cursor [:source-simple])
                  (get-in cursor [:selected])
                  :container-class-name ""
                  :selected-row-style :info
                  :page-size 5
                  :hover? true
                  :responsive? false
                  :header {:type :default
                           :start-sorted {:by :fecha
                                          :direction :down}
                           :columns [{:caption "Name"
                                      :field :name
                                      :text-alignment :left
                                      :sort true
                                      :sort-fn (fn [a b]
                                                 (compare (clojure.string/lower-case (:name a))
                                                          (clojure.string/lower-case (:name b))))}
                                     {:caption "Username"
                                      :field :username}

                                     {:caption "Fecha"
                                      :field :fecha
                                      :sort true
                                      :data-format :date}]})]]))))

(defn- grid-persist-paging
  [cursor owner]
  (reify
    om/IInitState
    (init-state [_]
      {:channel (chan)})

    om/IWillMount
    (will-mount [_]
      (go-loop []
        (let [msg (<! (om/get-state owner :channel))]
          (condp = (:event-type msg)
            :page-changed (om/update! cursor :current-page (:new-page msg))
            :row-selected ())
          (recur))))

    om/IRenderState
    (render-state [_ state]
      (html
        [:div.panel.panel-default
         [:div.panel-heading (str "Grid current page: "
                                  (get-in cursor [:current-page]))]
         [:div.panel-body
          (w/grid (get-in cursor [:source-simple])
                  (get-in cursor [:persist-paging])
                  :container-class-name ""
                  :selected-row-style :info
                  :page-size 5
                  :current-page (:current-page cursor)
                  :events-channel (:channel state)
                  :hover? true
                  :header {:type :default
                           :columns [{:caption "Username"
                                      :field :username}

                                     {:caption "Fecha"
                                      :field :fecha
                                      :sort true
                                      :data-format :date}]})]]))))

(defn- multiselect
  [cursor owner]
  (reify
    om/IInitState
    (init-state [_]
      {:errors nil
       :channel (chan)
       :feedback nil})

    om/IRenderState
    (render-state [_ {:keys [channel]}]
      (html
        [:div.panel.panel-default
         [:div.panel-heading (str "MultiSelect Grid (selected cursor value: "
                                  (map #(:name %) (get-in cursor [:multiselect])) " )")]
         [:div.panel-body
          (w/grid (get-in cursor [:source-simple])
                  (get-in cursor [:multiselect])
                  :multiselect? true
                  :container-class-name ""
                  :hover? true
                  :condensed? true
                  :bordered? false
                  :striped? false
                  :events-channel channel
                  :selected-row-style :info
                  :page-size 5
                  :header {:type :default
                           :start-sorted {:by :fecha
                                          :direction :down}
                           :columns [{:caption "Name"
                                      :field :name
                                      :text-alignment :left}
                                     {:caption "Username"
                                      :field :username}
                                     {:caption "Fecha"
                                      :field :fecha
                                      :data-format :date}]})]]))))


(defn grid-example
  [cursor owner]
  (reify
    om/IRenderState
    (render-state [this {:keys [channel]}]
      (html
        [:div
         (om/build grid-simple cursor)
         (om/build grid-persist-paging cursor)
         (om/build multiselect cursor)

         [:div.panel.panel-default
          [:div.panel-heading "Empty Grid"]
          [:div.panel-body
           (w/grid []
                   (get-in cursor [:selected])
                   :container-class-name ""
                   :page-size 2
                   :header {:type :default
                            :columns []})]]]))))

(defn grid-link-example
  [app owner]
  (reify
    om/IRenderState
    (render-state [this state]
      (dom/div #js {:className "panel panel-default"}
               (dom/div #js {:className "panel-heading"}
                        (str "Grid with custom cells (selected cursor value: " (:name (get-in app [:selected])) " )"))
               (dom/div #js {:className "panel-body"}
                        (dom/div #js {:className ""}
                                 (w/grid (get-in app [:source-custom-cell])
                                         (get-in app [:selected])
                                         :container-class-name ""
                                         :page-size 2
                                         :header {:type :default
                                                  :columns [{:caption "Name" :field :name}

                                                            {:caption "Username"
                                                             :field :username
                                                             :data-format :dom
                                                             :fn (fn [[id content]]
                                                                   (html
                                                                     [:a {:href (str "http://github.com/" content)}
                                                                      content]))}

                                                            {:caption "Registered Date"
                                                             :field :registered-date
                                                             :data-format :date
                                                             :date-formatter "yyyy/MM/dd"}

                                                            {:caption "Status"
                                                             :field :status
                                                             :col-span 2
                                                             :data-format :keyword
                                                             :options {:active "This user is active"
                                                                       :disabled "This user is disabled"}}]})))))))


(defmethod row-builder :users
  [row _ _]
  (reify
    om/IRenderState
    (render-state [this state]
                  (dom/div nil
                           (dom/label #js {:className ""} (str (:name row) " / " (:username row)))
                           (dom/a #js {:href (str "http://twitter.com/" (:username row))
                                       :className "pull-right"} "Twitter profile")))))

(defn grid-custom-row-sample
  [app owner]
  (reify
    om/IRenderState
    (render-state [this state]
                  (dom/div #js {:className "panel panel-default"}
                           (dom/div #js {:className "panel-heading"} "Grid Custom Row")
                           (dom/div #js {:className "panel-body"}
                                    (dom/div #js {:className ""}
                                             (w/grid (get-in app [:source-custom :rows])
                                                     (get-in app [:selected])
                                                     :header {:type :none})))))))


(defn grid-request-range-sample
  [app owner]
  (letfn [(get-data [start end]
            {:index start
             :total-rows 50000
             :rows (->> (range start (min 50000 end))
                        (map #(do {:item (str "Item " (inc %))}))
                        vec)})]
    (reify
      om/IInitState
      (init-state [_]
        {:channel (chan)})

      om/IWillMount
      (will-mount [_]
        (go
          (om/update! (om/get-props owner) :request-range (get-data 0 50))
          (loop []

            (let [cmd (<! (om/get-state owner :channel))]
              (when-not (= (:event-type cmd) :unmount)
                (condp = (:event-type cmd)
                  :request-range (go
                                   (<! (timeout 1000))
                                   (om/update! (om/get-props owner) :request-range (get-data (:start cmd)
                                                                                                  (:end cmd))))
                  nil)
                (recur))))))
      om/IRenderState
      (render-state [this {:keys [channel] :as state}]
        (dom/div #js {:className "panel panel-default"}
                 (dom/div #js {:className "panel-heading"} "Grid Request Range, 1s data retrieve delay ")
                 (dom/div #js {:className "panel-body"}
                          (dom/div #js {:className ""}
                                   (w/grid (get-in app [:request-range])
                                           (get-in app [:selected])
                                           :page-size 12
                                           :events-channel channel
                                           :header {:type :default
                                                    :columns [{:caption "Item" :field :item}]}))))))))
(defn grid-summarized-sample
  [app owner]

  (reify
    om/IRenderState
    (render-state [_ state]
      (dom/div #js {:className "panel panel-default"}
               (dom/div #js {:className "panel-heading"} "Grid Request Range, 1s data retrieve delay ")
               (dom/div #js {:className "panel-body"}
                        (dom/div #js {:className ""}
                                 (w/grid (get-in app [:source-summarized])
                                         (get-in app [:selected])
                                         :page-size 12
                                         :header {:type :default
                                                  :columns [{:caption "Name" :field :name}

                                                            {:caption "Username"
                                                             :field :username}

                                                            {:caption "Registered Date"
                                                             :field :fecha
                                                             :data-format :date
                                                             :date-formatter "yyyy/MM/dd"}

                                                            {:caption "Ammount"
                                                             :field :total
                                                             :summarize (fn [acc row]
                                                                           (+ acc (:total row)))
                                                             :summary-render (fn [value]
                                                                               [:div.text-info
                                                                                [:label value]
                                                                                [:span.glyphicon.glyphicon-euro]]
                                                                               )
                                                             }]}
                                         :summarized? true)))))))
