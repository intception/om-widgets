(ns intception-widgets.tab
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs.reader :as reader]))

(defn- tab-header
  [page]
  (reify
    om/IRenderState
    (render-state [this state]
        (dom/li #js {:className (if (= (:current-page page) (:index page)) "active" "inactive")}
          (dom/a #js {:href "#" :onClick (fn[e]
                                           (om/set-state! (:parent-owner page) :current-page (:index page))
                                           false)}
                (:label page))))))

(defn- tab-page
  [page]
  (reify
    om/IRenderState
    (render-state [this state]
        (dom/div #js {:className (if (= (:current-page page) (:index page)) "active-tab" "inactive-tab")}
                  (:content page)))))

(defn- tab-component
  [_ owner]
 (reify
   om/IRenderState
   (render-state [this {:keys [current-page pages id]}]
     (let [opts (map #(merge % {:current-page current-page
                                :parent-owner owner
                                :index %2})  pages (range))]
      (dom/div #js {:className "tab" :id id}
        (apply dom/ul #js {:className "nav nav-tabs" :role "tablist"}
              (om/build-all tab-header opts))
        (apply dom/div nil
               (om/build-all tab-page opts)))))))


(defn tab
  [id & pages]
  (om/build tab-component nil {:state {:id id :pages pages}
                               :init-state {:current-page 0}}))
