(ns om-widgets.slider
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [sablono.core :as html :refer-macros [html]]
            [om-widgets.utils :as utils]))

;; ---------------------------------------------------------------------
;; TODO
;; Support datalist
;;  <datalist id=volsettings>
;;    <option>0</option>
;;    <option>20</option>
;;    <option>40</option>
;;    <option>60</option>
;;    <option>80</option>
;;    <option>100</option>
;;  </datalist>

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
                 :value (or ((:path state) cursor) 0)
                 :onChange (fn [e]
                             (let [value (js/parseInt (.. e -target -value))]
                               (om/update! cursor (:path state) value)
                               (when (:onChange state) ((:onChange state) value))))}]))))

(defn slider
  [cursor path {:keys [min max step onChange disabled class-name] :as opts
                :or {min "" max "" step "" disabled false}}]
  (om/build input cursor {:state (merge {:path path} opts)}))
