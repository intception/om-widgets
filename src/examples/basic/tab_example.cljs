(ns examples.basic.tab-example
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [sablono.core :as html :refer-macros [html]]
            [om-widgets.core :as w]))


(defn content
  [cursor owner {:keys [title]}]
  (reify
    om/IRenderState
    (render-state [_ _]
      (html
       [:div.panel.panel-default
        [:div.panel-body
         [:div.jumbotron
          [:h4 title]]]]))))

(defn tab-example
  [cursor owner]
  (reify
    om/IDisplayName
    (display-name [_] "TabSample")

    om/IRenderState
    (render-state [this state]
      (html
       [:div
        (apply w/tab cursor :selected-variant {}
               [{:content #(om/build content cursor {:opts {:title "Inbox"}})
                 :icon :inbox
                 :label "Inbox"}
                {:content #(om/build content cursor {:opts {:title "Config"}})
                 :icon :wrench
                 :label "Config"}
                {:content #(om/build content cursor {:opts {:title "Profile"}})
                 :icon :user
                 :label "Profile"
                 :disabled true}])]))))
