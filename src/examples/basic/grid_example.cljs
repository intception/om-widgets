(ns examples.basic.grid-example
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [om-widgets.grid :refer [row-builder]]
            [sablono.core :as html :refer-macros [html]]
            [om-widgets.core :as w]))


(defn grid-example
  [app owner]
  (reify
    om/IDisplayName
    (display-name[_] "GridSample")

    om/IRenderState
    (render-state [this state]
      (html
        [:div

         [:div.panel.panel-default
          [:div.panel-heading (str "Grid (selected cursor value: "
                                   (get-in app [:selected :name]) " )")]
          [:div.panel-body
           (w/grid (get-in app [:source-simple])
                   (get-in app [:selected])
                   :container-class-name ""
                   :selected-row-style :info
                   :page-size 5
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
                                       :data-format :date}]})]]

         [:div.panel.panel-default
          [:div.panel-heading (str "MultiSelect Grid (selected cursor value: "
                                   (map #(:name %) (get-in app [:multiselect])) " )")]
          [:div.panel-body
           (w/grid (get-in app [:source-simple])
                   (get-in app [:multiselect])
                   :multiselect? true
                   :container-class-name ""
                   :hover? true
                   :condensed? true
                   :bordered? false
                   :striped? false
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
                                       :data-format :date}]})]]

         [:div.panel.panel-default
          [:div.panel-heading "Empty Grid"]
          [:div.panel-body
           (w/grid []
                   (get-in app [:selected])
                   :container-class-name ""
                   :page-size 2
                   :header {:type :default
                            :columns []})]]]))))

(defn grid-link-example
  [app owner]
  (reify
    om/IDisplayName
    (display-name[_] "GridWithLinkSample")

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
    om/IDisplayName
    (display-name[_] "CustomRow")
    om/IRenderState
    (render-state [this state]
                  (dom/div nil
                           (dom/label #js {:className ""} (str (:name row) " / " (:username row)))
                           (dom/a #js {:href (str "http://twitter.com/" (:username row))
                                       :className "pull-right"} "Twitter profile")))))

(defn grid-custom-row-sample
  [app owner]
  (reify
    om/IDisplayName
    (display-name[_] "GridCustomRowSample")

    om/IRenderState
    (render-state [this state]
                  (dom/div #js {:className "panel panel-default"}
                           (dom/div #js {:className "panel-heading"} "Grid Custom Row")
                           (dom/div #js {:className "panel-body"}
                                    (dom/div #js {:className ""}
                                             (w/grid (get-in app [:source-custom :rows])
                                                     (get-in app [:selected])
                                                     :header {:type :none})))))))