(ns intception-widgets.utils
  (:require [om.core :as om :include-macros true]
     [om.dom :as dom :include-macros true]))


(defn om-update! [tgt key value]
  (if (= nil key)
    (om/update! tgt value)
    (try
      (om/set-state! tgt key value)
      (catch js/Error _
        (om/update! tgt key value)))))


(defn om-get [tgt key]
  (try
    (om/get-state tgt key)
    (catch js/Error _
      (let [ks (if (sequential? key) key [key])]
           (get-in tgt ks)))))
