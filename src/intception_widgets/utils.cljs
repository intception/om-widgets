(ns intception-widgets.utils
  (:require [om.core :as om :include-macros true]
     [om.dom :as dom :include-macros true]))

(defn om-update! [tgt key value]
  (if (= nil key)
    (om/update! tgt value)
    (if (satisfies? om/ISetState tgt)
      (if (fn? value)
        (om/update-state! tgt key value)
        (om/set-state! tgt key value))
      (if (fn? value)
          (if-not (= nil key)
            (om/transact! tgt key value)
            (om/transact! tgt value))
          (om/update! tgt key value)))))

(defn om-get [tgt key]
  (if (satisfies? om/IGetState tgt)
    (om/get-state tgt key)
      (let [ks (if (sequential? key) key [key])]
           (get-in tgt ks))))

