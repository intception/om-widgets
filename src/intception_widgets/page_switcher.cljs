(ns intception-widgets.page-switcher
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs.reader :as reader]))


(defn- page-switcher-page
  [page]
  (reify
    om/IRenderState
    (render-state [this state]
        (dom/div #js {:className (if (= (:current-page page) (:index page)) "active-page" "inactive-page")}
                 (dom/div #js {:className "page-header"}
                          (:label page))
                 (dom/div #js {:className "page-content"}
                  (:content page))))))

(defn- page-switcher-component
  [_ owner]
 (reify
   om/IRenderState
   (render-state [this {:keys [current-page pages id]}]
     (let [opts (map #(merge % {:current-page current-page
                                :parent-owner owner
                                :index %2})  pages (range))]
      (dom/div #js {:className "om-widgets-page-switcher" :id id}
        (om/build page-switcher-page (nth opts current-page)))))))


(defn page-switcher
  [id current-page & pages]
  (om/build page-switcher-component nil {:state {:id id
                                                 :pages pages
                                                 :current-page current-page}}))
