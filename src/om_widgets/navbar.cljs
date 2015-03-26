(ns om-widgets.navbar
  (:require [om-widgets.dropdown :refer [dropdown EntrySchema DividerSchema]]
            [schema.core :as s :include-macros true]
            [om.core :as om :include-macros true]
            [om-widgets.utils :as u]
            [sablono.core :as html :refer-macros [html]]
            [om.dom :as dom :include-macros true]))


;; TODO support collapse button
;; (dom/button #js {:className "navbar-toggle collapsed" :type "button"}
;;                         (dom/span #js {:className "sr-only"} "Toggle navigation")
;;                         (dom/span #js {:className "icon-bar"}))

(defn- entry-component
  [{:keys [cursor entry]} owner]
  (reify
    om/IDisplayName
    (display-name [_] "NavBarEntry")

    om/IRenderState
    (render-state [_ {:keys [on-selection active-path active]}]
      (html
        [:li {:class (when (= (get-in cursor [active-path])
                              (:id entry))
                       "active")}

        (if (:items entry)
          (dropdown cursor (->> {:id (:id entry)
                                 :type :menu
                                 :title (:text entry)
                                 :on-selection on-selection
                                 :items (:items entry)}))
          [:a (->> {}
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
                                          (on-selection (:id @entry)))} %)
                       %)))
           [:span {:class (:iconClassName entry)}]
           (:text entry)])]))))

(defn- navbar-nav
  [{:keys [cursor entries]} owner]
  (reify
    om/IDisplayName
    (display-name [_] "NavBarNav")

    om/IRenderState
    (render-state [this {:keys [active-path on-selection]}]
      (apply dom/ul #js {:className (str "nav navbar-nav"
                                         (when (:right-position (first entries))
                                           " navbar-right"))}
             (map #(om/build entry-component {:cursor cursor
                                              :entry %}
                             {:state {:active-path active-path
                                      :on-selection on-selection}})
                  entries)))))

(defn- nav-header
  [app owner]
  (reify
    om/IDisplayName
    (display-name [_] "NavBarHeader")

    om/IRenderState
    (render-state [this state]
      (html
        [:div.navbar-header
         [:a.navbar-brand {:href "#"}
          [:img {:src (:brand-image-url state)
                 :alt (or (:brand-title state) "brand-logo")
                 :height (if (:brand-image-expanded state) "100%" "")}]
          (str " " (:brand-title state))]]))))

(defn navbar-container
  [cursor owner]
  (reify
    om/IDisplayName
    (display-name [_] "NavBarContainer")

    om/IRenderState
    (render-state [this state]
      (html
        [:nav {:class ["navbar"
                       "navbar-default"
                       (when (:fixed-top state) "navbar-fixed-top")]}

         [:div {:class ["container"
                        (when (= (:container state) :fluid) "-fluid")]}]

         (om/build nav-header
                   cursor
                   {:state {:brand-image-url (:brand-image-url state)
                            :brand-image-expanded (:brand-image-expanded state)
                            :brand-title (:brand-title state)}})

         (u/make-childs [:div.navbar-collapse]
                        (map #(om/build navbar-nav {:cursor cursor
                                                    :entries %}
                                        {:state {:active-path (:active-path state)
                                                 :on-selection (:on-selection state)}})
                             (:menu-items cursor)))]))))

;; ---------------------------------------------------------------------
;; Schema

(def NavbarEntry
  {:text s/Str
   :id s/Keyword
   (s/optional-key :url) s/Str
   (s/optional-key :className) s/Str
   (s/optional-key :iconClassName) s/Str})

(def NavbarDropdownEntry
  {:text s/Str
   :id s/Keyword
   :items [(s/either EntrySchema DividerSchema)]
   (s/optional-key :right-position) s/Bool})

(def NavbarSchema
  {:items [[(s/either NavbarEntry NavbarDropdownEntry)]]
   :brand-image-url s/Str
   :brand-title s/Str
   (s/optional-key :fixed-top) s/Bool
   (s/optional-key :brand-image-expanded) s/Bool
   (s/optional-key :container) (s/enum :default :fluid)
   (s/optional-key :on-selection) (s/pred fn?)})


;; ---------------------------------------------------------------------
;; Public

(defn navbar
  [app active-path {:keys [brand-image-url brand-title on-selection] :as options}]
  (s/validate NavbarSchema options)
  (om/build navbar-container app {:state (merge {:active-path active-path} options)}))
