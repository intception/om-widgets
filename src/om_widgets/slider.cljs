(ns om-widgets.slider
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [sablono.core :as html :refer-macros [html]]
            [om-widgets.utils :as utils]))

(defn- input
  [cursor owner]
  (reify
    om/IRenderState
    (render-state
      [this state]
      (html
        [:input {:type "range"
                 :disabled (:disabled state)
                 :class (:class-name state)
                 :min (:min state)
                 :max (:max state)
                 :step (:step state)
                 :defaultValue ((:path state) cursor)
                 :onChange (fn [e]
                             (let [value (.. e -target -value)]
                               (om/update! cursor (:path state) value)
                               (when (:onChange state) ((:onChange state) value))))}]))))

(defn slider
  [cursor path {:keys [min max step onChange disabled class-name] :as opts
                :or {min "" max "" step "" disabled false}}]
  (om/build input cursor {:state (merge {:path path} opts)}))
