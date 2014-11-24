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

(defn- entry [app owner]
  (reify
    om/IDisplayName
    (display-name[_] "NavBarEntry")

    om/IRenderState
    (render-state [_ {:keys [entry selected]}]
                  (dom/li #js {:className (when (= selected (:id entry)) "active")}
                          (if (:items entry)
                            (dropdown app {:id (:id entry)
                                           :type :menu
                                           :title (:text entry)
                                           :items (:items entry)})
                            (dom/a #js {:href (str "#/" (name (:id entry)))
                                        :className (:className entry)}
                                   (dom/span #js {:className (:iconClassName entry)})
                                   (:text entry)))))))

(defn- navbar-nav [app owner]
  (reify
    om/IDisplayName
    (display-name[_] "NavBarNav")

    om/IRenderState
    (render-state [this {:keys [navbar selected]}]
                  (apply dom/ul #js {:className (str "nav navbar-nav"
                                                     (when (:right-position (first navbar))
                                                       " navbar-right"))}
                         (map #(om/build entry app {:state {:selected selected :entry %}})
                              navbar)))))

(defn- nav-header [app owner]
  (reify
    om/IDisplayName
    (display-name[_] "NavBarHeader")

    om/IRenderState
    (render-state [this state]
                  (dom/div #js {:className "navbar-header"}
                           (dom/a #js {:className "navbar-brand" :href "#"}
                                  (dom/img #js {:src (:brand-image-url state)
                                                :alt (:brand-title state)
                                                :height "100%"})
                                  (str " " (:brand-title state)))))))

(defn- navbar-container [app owner]
  (reify
    om/IDisplayName
    (display-name[_] "NavBarContainer")

    om/IRenderState
    (render-state [this {:keys [container items brand-image-url brand-title selected] :as state}]
                  (dom/nav #js {:className "navbar navbar-default" :role "navigation" }
                           (dom/div #js {:className (str "container"
                                                         (when (= container :fluid) "-fluid"))}
                                    (om/build nav-header app {:state {:brand-image-url brand-image-url
                                                                      :brand-title brand-title}})
                                    (apply dom/div #js {:className "navbar-collapse"}
                                           (map #(om/build navbar-nav app {:state {:selected selected
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
   :selected s/Keyword
   :brand-image-url s/Str
   (s/optional-key :container) (s/enum :default :fluid)
   :brand-title s/Str})


;; ---------------------------------------------------------------------
;; Public

(defn navbar
  [app {:keys [items selected brand-image-url brand-title] :as options}]
  (s/validate NavbarSchema options)
  (om/build navbar-container app {:state options}))
