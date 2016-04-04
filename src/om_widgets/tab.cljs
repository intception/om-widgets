(ns om-widgets.tab
  (:require-macros [pallet.thread-expr :as th])
  (:require [om.core :as om :include-macros true]
            [om-widgets.utils :as u]
            [sablono.core :refer-macros [html]]
            [schema.core :as s]))


(defn- tab-header
  [page]
  (reify
    om/IRenderState
    (render-state [_ _]
      (html
        [:li {:class (cond
                       (= (:current-page page) (:id page)) "active"
                       (:disabled page) "disabled"
                       :else "inactive")}
         (if (fn? (:label page))
           ((:label page))
           [:a {:class "om-widgets-tab-item"
                :onClick (fn [e]
                           (when (not (:disabled page))
                             (let [parent-owner (:parent-owner page)
                                   on-change (om/get-state parent-owner :on-change)]
                               (when (and on-change
                                          (not= (u/om-get (om/get-props parent-owner) :current-page)
                                                page))
                                 (on-change (:id page)))

                               (u/om-update! (om/get-props parent-owner) (om/get-state parent-owner :path) (:id page))

                               (when (u/atom? (om/get-props parent-owner))
                                 (om/refresh! parent-owner))))
                           (.preventDefault e))}
            (when (:icon page)
              [:i {:class (str "glyphicon glyphicon-" (name (:icon page)))}])

            (if (vector? (:label page))
              (html (:label page))
              (str (when (:icon page) "  ") (:label page)))])]))))

(defn- tab-page
  [page _]
  (reify
    om/IRenderState
    (render-state [_ _]
      (html
        [:div {:class "om-widgets-active-tab"}
         (when (:content page)
           (if (fn? (:content page))
             ((:content page))
             (:content page)))]))))

(defn- tab-component
  [cursor owner]
  (reify
    om/IRenderState
    (render-state [_ {:keys [pages id right-panel path class-name]}]
      (let [page-id (u/om-get cursor path)
            page-config (->> pages (filter #(and (:id %) (= (:id %) page-id))) first)
            active-page (or page-config (first pages))
            header-opts (map #(merge % {:current-page (:id active-page)
                                        :parent-owner owner})
                             pages)]

        (html
          [:div.om-widgets-tab (-> {:class class-name}
                                   (th/when-> id (merge {:id id})))

           [:div.om-widgets-top-row
            (u/make-childs
              [:ul.nav.nav-tabs.om-widgets-nav.om-widgets-nav-tabs]
              (conj (om/build-all tab-header header-opts)
                    (when right-panel
                      [:li.om-widgets-right-panel right-panel])))]

           [:div
            (om/build tab-page active-page)]])))))


;; ---------------------------------------------------------------------
;; Schema
(def Pages
  [{:label (s/either (s/pred fn?) [s/Any] s/Str)
    (s/optional-key :id) s/Any
    (s/optional-key :content) s/Any
    (s/optional-key :disabled) s/Bool
    (s/optional-key :icon) s/Keyword}])

(def TabSchema
  {(s/optional-key :id) s/Str
   (s/optional-key :on-change) (s/pred fn?)
   (s/optional-key :right-panel) s/Any
   (s/optional-key :class-name) s/Str})


;; ---------------------------------------------------------------------
;; Public
(defn tab
  [cursor path {:keys [id on-change right-panel class-name] :as opts} & pages]
  (s/validate TabSchema opts)
  (s/validate Pages pages)
  (om/build tab-component cursor {:state {:id id
                                          :path path
                                          :pages pages
                                          :class-name class-name
                                          :on-change on-change
                                          :right-panel right-panel}}))
