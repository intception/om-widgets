(ns om-widgets.switcher
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [schema.core :as s :include-macros true]
            [sablono.core :as html :refer-macros [html]]
            [cljs.core.async :refer [put! chan <! alts! timeout close!]]
            [om-widgets.utils :as utils]))


(defn- build-label-class [cursor cursor-path value]
  (str "btn btn-default" (when (= (get-in cursor [cursor-path]) value) " active")))

(defn item
  [cursor owner]
  (reify
    om/IRenderState
    (render-state
      [this {:keys [value text cursor-path channel] :as state}]
      (html
        [:label {:class (build-label-class cursor cursor-path value)}
         [:input {:type "radio"
                  :key text
                  :onChange (fn [e]
                              (when channel
                                (put! channel value))
                              (om/update! cursor cursor-path value))}
          text]]))))

(defn switcher
  [cursor owner]
  (reify
    om/IRenderState
    (render-state
      [this {:keys [options cursor-path channel classes] :as state}]
      (html
        (utils/make-childs [:div {:class ["btn-group" (or classes "")]
                                  :data-toggle "buttons"}]
                           (map #(om/build item
                                           cursor
                                           {:state {:value (:value %)
                                                    :text (:text %)
                                                    :channel channel
                                                    :cursor-path cursor-path}})
                                options))))))

;; ---------------------------------------------------------------------
;; Schema

(def OptionsSchema
  [{:value s/Any
    :text s/Str}])

;; ---------------------------------------------------------------------
;; Public

(defn switch
  [cursor cursor-path {:keys [options channel classes]}]
  (s/validate OptionsSchema options)
  (om/build switcher cursor {:state {:cursor-path cursor-path
                                     :options options
                                     :classes classes
                                     :channel channel}}))
