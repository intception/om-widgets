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
          (dom/a #js {:className "tab-item"
                      :href "#"
                      :onClick (fn[e]
                                  (let [parent-owner (:parent-owner page)
                                        on-change (om/get-state parent-owner :on-change)]
                                  (when (and on-change (not= (om/get-state parent-owner :current-page) (:index page)))
                                        (on-change (:index page)))
                                  (om/set-state! parent-owner :current-page (:index page)))

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
   (render-state [this {:keys [current-page pages id right-panel]}]
     (let [opts (map #(merge % {:current-page current-page
                                :parent-owner owner
                                :index %2})  pages (range))]
      (dom/div #js {:className "tab" :id id}
        (dom/div #js {:className "top-row"}
          (apply dom/ul #js {:className "nav nav-tabs" :role "tablist"}
                (conj (om/build-all tab-header opts)
                      (when right-panel
                        (dom/li #js {:className "right-panel"}
                          right-panel)))))
        (dom/div nil
               (om/build  tab-page (nth opts current-page))))))))


(defn tab
  [{:keys [id current-page on-change right-panel] :or {current-page 0} } & pages]
  (om/build tab-component nil {:state {:id id :pages pages :current-page current-page :on-change on-change :right-panel right-panel}}))

