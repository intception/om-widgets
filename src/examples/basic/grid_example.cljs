(ns examples.basic.grid-example
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [om-widgets.grid :refer [row-builder cell-builder]]
            [sablono.core :as html :refer-macros [html]]
            [om-widgets.core :as w]))


(defn grid-example
  [app owner]
  (reify
    om/IDisplayName
    (display-name[_] "GridSample")

    om/IRenderState
    (render-state [this state]
                  (dom/div #js {:className "panel panel-default"}
                           (dom/div #js {:className "panel-heading"} (str "Grid"
                                                                          " (selected cursor value: "
                                                                          (:name (get-in app [:selected]))
                                                                          " )"))
                           (dom/div #js {:className "panel-body"}
                                    (dom/div #js {:className ""}
                                             (w/grid (get-in app [:source-simple])
                                                     (get-in app [:selected])
                                                     :container-class-name ""
                                                     :page-size 2
                                                     :header {:type :default
                                                              :columns (get-in app [:columns])})))))))

(defn grid-link-example
  [app owner]
  (reify
    om/IDisplayName
    (display-name[_] "GridWithLinkSample")

    om/IRenderState
    (render-state [this state]
      (dom/div #js {:className "panel panel-default"}
               (dom/div #js {:className "panel-heading"}
                        (str "Grid with link (selected cursor value: " (:name (get-in app [:selected])) " )"))
               (dom/div #js {:className "panel-body"}
                        (dom/div #js {:className ""}
                                 (w/grid (get-in app [:source-simple])
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
                                                                      content]))}]})))))))


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