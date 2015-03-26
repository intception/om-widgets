(ns examples.basic.dropdown-example
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [sablono.core :as html :refer-macros [html]]
            [om-widgets.core :as w]))


(defn dropdown-example
  [cursor owner]
  (reify
    om/IDisplayName
    (display-name [_] "DropdownSample")

    om/IRenderState
    (render-state [this state]
      (dom/div #js {:className "panel panel-default"
                    :onClick #(println "preventing dropdown click propagation...")}
               (dom/div #js {:className "panel-heading"}
                        (str "Dropdown (selected cursor value: "
                             (get-in cursor [:selected-dropdown])
                             " )"))
               (dom/div #js {:className "panel-body"}
                        (w/dropdown cursor
                                    {:id :testing
                                     :title "Item Actions"
                                     :on-selection #(om/update! cursor [:selected-dropdown] %)
                                     :size :sm
                                     :stop-propagation true
                                     :items (get-in cursor [:items])}))))))