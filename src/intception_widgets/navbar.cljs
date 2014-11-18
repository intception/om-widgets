(ns intception-widgets.navbar
  (:require
    [intception-widgets.dropdown :refer [dropdown-menu]]
    [om.core :as om :include-macros true]
    [om.dom :as dom :include-macros true]))

;; TODO support collapse button
;; (dom/button #js {:className "navbar-toggle collapsed" :type "button"}
;;                         (dom/span #js {:className "sr-only"} "Toggle navigation")
;;                         (dom/span #js {:className "icon-bar"}))

(defn- entry [app owner]
  (reify
    om/IDisplayName
    (display-name[_] "NavBarEntry")

    om/IRenderState
    (render-state [this {:keys [entry selected]}]

                  (dom/li #js {:className (when (= selected (:id entry)) "active")}

                          (if (:items entry)
                            (dropdown-menu app (:id entry) (:text entry) (:items entry))
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
                  (apply dom/ul #js {:className (str "nav navbar-nav" (when (:right-position (first navbar))
                                                                        " navbar-right"))}
                          (map #(om/build entry app {:state {:selected selected :entry %}})
                               navbar)))))

(defn- nav-header []
  (reify
    om/IDisplayName
    (display-name[_] "NavBarHeader")

    om/IRenderState
    (render-state [this state]
                  (dom/div #js {:className "navbar-header"}
                           (dom/a #js {:className "navbar-brand" :href "#"}
                                  (dom/img #js {:src (:brand-image-url state) :alt (:brand-title state)
                                                :height "100%"}) (str " " (:brand-title state)))))))

(defn- navbar-container [app owner]
  (reify
    om/IDisplayName
    (display-name[_] "NavBarContainer")

    om/IRenderState
    (render-state [this state]
                  (dom/nav #js {:className "navbar navbar-default" :role "navigation" }
                           (dom/div #js {:className "container" }
                                    (om/build nav-header app {:state {:brand-image-url (:brand-image-url state)
                                                                      :brand-title (:brand-title state)}})
                                    (apply dom/div #js {:className "navbar-collapse"}
                                           (map #(om/build navbar-nav app {:state {:selected (:selected state)
                                                                               :navbar %}})
                                                (:items state))))))))

(defn navbar
  "items example:

  {:home {:text 'Home'}
  :about {:text 'About'
  :items [{:team 'Team'}]}}
  "
  [app {:keys [items selected brand-image-url brand-title] :as options}]
  (om/build navbar-container app {:state options}))
