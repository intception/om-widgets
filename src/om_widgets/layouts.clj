(ns widgets.layouts
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]))


(defmacro row
  [& cols]
  `(dom/div (cljs.core/clj->js {:className "row"})
    ~@cols))

(defmacro column
  [width & fields]
  `(dom/div nil ~@ (for[f fields]
                (reverse (into '() (assoc-in (vec f)
                                             [2 :extra-class]
                                             (str "col-md-" width)))))))

(defmacro dynamic-row
  [& fields]
  `(dom/div (cljs.core/clj->js {:className "row"})
            ~@(for [f fields]
                (reverse (into '() (assoc-in (vec f)
                                             [2 :extra-class]
                                             (str "col-md-"
                                                  (int (/ 12 (count fields))))))))))
