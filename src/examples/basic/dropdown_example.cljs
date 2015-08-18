(ns examples.basic.dropdown-example
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [sablono.core :as html :refer-macros [html]]
            [om-widgets.core :as w]))


(defn dropdown-example
  [cursor owner]
  (reify
    om/IRenderState
    (render-state [this state]
      (html
        [:div.panel.panel-default
         [:div.panel-heading (str "Selected cursor value: " (get-in cursor [:selected-dropdown]))]

         [:div.panel-body

          ;; ---------------------------------------------------------------------
          ;; Sizes
          (w/dropdown cursor
                      {:id :testing
                       :title "Small Dropdown Button"
                       :korks [:selected-dropdown]
                       :size :sm
                       :items (get-in cursor [:default])}) [:hr]

          (w/dropdown cursor
                      {:id :testing
                       :title "Normal with URLS"
                       :korks [:selected-dropdown]
                       :items (get-in cursor [:urls])}) [:hr]

          ;; ---------------------------------------------------------------------
          ;; Callback
          (w/dropdown cursor
                      {:id :testing
                       :title "on-selection Callback"
                       :on-selection #(.alert js/window (str "Selected value: " %))
                       :items (get-in cursor [:urls])}) [:hr]

          ;; ---------------------------------------------------------------------
          ;; Disabled
          (w/dropdown cursor
                      {:id :testing
                       :title "Disabled entries"
                       :korks [:selected-dropdown]
                       :items (get-in cursor [:disabled])}) [:hr]

          ;; ---------------------------------------------------------------------
          ;; Dividers
          (w/dropdown cursor
                      {:id :testing
                       :title "with Dividers"
                       :korks [:selected-dropdown]
                       :items (get-in cursor [:dividers])}) [:hr]

          ;; ---------------------------------------------------------------------
          ;; Links
          (w/dropdown cursor
                      {:id :testing
                       :title "Small Dropdown Link"
                       :as-link? true
                       :korks [:selected-dropdown]
                       :size :sm
                       :items (get-in cursor [:default])})

          (w/dropdown cursor
                      {:id :testing
                       :title "Normal Dropdown Link"
                       :as-link? true
                       :korks [:selected-dropdown]
                       :items (get-in cursor [:default])})

          (w/dropdown cursor
                      {:id :testing
                       :title "Big Dropdown Link"
                       :as-link? true
                       :korks [:selected-dropdown]
                       :size :lg
                       :items (get-in cursor [:default])})[:hr]

          ;; ---------------------------------------------------------------------
          ;; Icons
          (w/dropdown cursor
                      {:id :testing
                       :title "with icon"
                       :icon "glyphicon glyphicon-calendar"
                       :korks [:selected-dropdown]
                       :items (get-in cursor [:default])})]]))))