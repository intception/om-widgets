(ns examples.basic.tab-example
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [sablono.core :as html :refer-macros [html]]
            [om-widgets.core :as w]))


(defn a
  [cursor owner]
  (reify
    om/IWillMount
    (will-mount [_]
      (println "Mounting A"))

    om/IRenderState
    (render-state [_ _]
      (html
       [:div
        "TAB A"]))))

(defn b
  [cursor owner]
  (reify
    om/IWillMount
    (will-mount [_]
      (println "Mounting B"))

    om/IRenderState
    (render-state [_ _]
      (html
       [:div
        "TAB B"]))))


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
               [{:content #(om/build a cursor)
                 :label "A"}
                {:content #(om/build b cursor)
                 :label "B"
                 :disabled true}])]))))
