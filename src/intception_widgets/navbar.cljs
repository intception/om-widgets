(ns intception-widgets.navbar
  (:require [intception-widgets.dropdown :refer [dropdown EntrySchema DividerSchema]]
            [schema.core :as s :include-macros true]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]))

(enable-console-print!)

;; TODO support collapse button
;; (dom/button #js {:className "navbar-toggle collapsed" :type "button"}
;;                         (dom/span #js {:className "sr-only"} "Toggle navigation")
;;                         (dom/span #js {:className "icon-bar"}))

(defn- entry [cursor owner]
  (reify
    om/IDisplayName
    (display-name[_] "NavBarEntry")

    om/IRenderState
    (render-state [_ {:keys [entry set-path on-selection]}]
                  (dom/li #js {:className (when (= (get-in cursor [set-path])
                                                   (:id entry)) "active")}
                          (if (:items entry)
                            (dropdown cursor set-path {:id (:id entry)
                                                       :type :menu
                                                       :title (:text entry)
                                                       :on-selection on-selection
                                                       :items (:items entry)})
                            (dom/a (cljs.core/clj->js (->> {}
                                                           ;; TODO write a macro like pallet.thread-expr
                                                           ;; but that works on clojurescript
                                                           (#(if (:className entry)
                                                               (merge {:className (:className entry)} %)
                                                               %))
                                                           (#(if (:url entry)
                                                               (merge {:href (:url entry)} %)
                                                               %))
                                                           (#(if on-selection
                                                               (merge {:onClick (fn [e]
                                                                                  (om/update! cursor set-path (:id @entry))
                                                                                  (on-selection (:id @entry)))} %)
                                                               %))))
                                   (dom/span #js {:className (:iconClassName entry)})
                                   (:text entry)))))))

(defn- navbar-nav [app owner]
  (reify
    om/IDisplayName
    (display-name[_] "NavBarNav")

    om/IRenderState
    (render-state [this {:keys [navbar set-path on-selection]}]
                  (apply dom/ul #js {:className (str "nav navbar-nav"
                                                     (when (:right-position (first navbar))
                                                       " navbar-right"))}
                         (map #(om/build entry app {:state {:set-path set-path
                                                            :entry %
                                                            :on-selection on-selection}})
                              navbar)))))

(defn- nav-header [app owner]
  (reify
    om/IDisplayName
    (display-name[_] "NavBarHeader")

    om/IRenderState
    (render-state [this {:keys [brand-title brand-image-url brand-image-expanded] :as state}]
                  (dom/div #js {:className "navbar-header"}
                           (dom/a #js {:className "navbar-brand" :href "#"}
                                  (dom/img #js {:src brand-image-url
                                                :alt (or brand-title "brand-logo")
                                                :height (if brand-image-expanded "100%" "")})
                                  (str " " brand-title))))))

(defn- navbar-container [app owner]
  (reify
    om/IDisplayName
    (display-name[_] "NavBarContainer")

    om/IRenderState
    (render-state [this {:keys [container items brand-image-url brand-title brand-image-expanded set-path on-selection] :as state}]
                  (dom/nav #js {:className (str "navbar navbar-default"
                                                (when (:fixed-top state) " navbar-fixed-top"))}
                           (dom/div #js {:className (str "container"
                                                         (when (= container :fluid) "-fluid"))}
                                    (om/build nav-header app {:state {:brand-image-url brand-image-url
                                                                      :brand-image-expanded brand-image-expanded
                                                                      :brand-title brand-title}})
                                    (apply dom/div #js {:className "navbar-collapse"}
                                           (map #(om/build navbar-nav app {:state {:set-path set-path
                                                                                   :on-selection on-selection
                                                                                   :navbar %}})
                                                items)))))))


;; ---------------------------------------------------------------------
;; Schema

(def NabvarNavSchema
  {:text s/Str
   :id s/Keyword
   (s/optional-key :className) s/Str
   (s/optional-key :iconClassName) s/Str
   :url s/Str})

(def NabvarNavDropdownSchema
  {:text s/Str
   :id s/Keyword
   (s/optional-key :right-position) s/Bool
   :items [(s/either EntrySchema DividerSchema)]})

(def NavbarSchema
  {:items [[(s/either NabvarNavSchema NabvarNavDropdownSchema)]]
   :brand-image-url s/Str
   :brand-title s/Str
   (s/optional-key :fixed-top) s/Bool
   (s/optional-key :brand-image-expanded) s/Bool
   (s/optional-key :container) (s/enum :default :fluid)
   (s/optional-key :on-selection) (s/pred fn?)})


;; ---------------------------------------------------------------------
;; Public

(defn navbar
  [app set-path {:keys [items brand-image-url brand-title on-selection] :as options}]
  (s/validate NavbarSchema options)
  (om/build navbar-container app {:state (merge {:set-path set-path} options)}))
