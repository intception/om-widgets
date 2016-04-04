(ns examples.basic.tab-example
  (:require [om.core :as om :include-macros true]
            [sablono.core :refer-macros [html]]
            [om-widgets.core :as w]))


(defn content
  [_ _ {:keys [title]}]
  (reify
    om/IRenderState
    (render-state [_ _]
      (html
        [:div.panel.panel-default
         [:div.panel-body
          [:div.jumbotron
           [:h4 title]]]]))))

(defn tab-example
  [cursor _]
  (reify
    om/IRenderState
    (render-state [_ _]
      (html
        [:div
         (w/tab cursor :selected-tab {}
                {:content #(om/build content cursor {:opts {:title "Inbox"}})
                 :icon :inbox
                 :id :inbox
                 :label "Inbox"}

                {:content #(om/build content cursor {:opts {:title "Config"}})
                 :icon :wrench
                 :id :config
                 :label "Config"}

                {:content #(om/build content cursor {:opts {:title "Profile"}})
                 :icon :user
                 :id :user
                 :label [:span " Profile"]
                 :disabled true})]))))
