(ns om-widgets.runner-test
  (:require-macros [cljs.test :refer (run-tests run-all-tests)])
  (:require [cljs.test]))

(def success 0)

(defn ^:export run []
  (.log js/console "om-widgets test started.")
  (run-all-tests)
  success)