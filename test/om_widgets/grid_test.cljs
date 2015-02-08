(ns om-widgets.grid-test
  (:require-macros [cemerick.cljs.test :refer (is deftest with-test run-tests testing test-var)])
  (:require [cemerick.cljs.test :as t]
            [om-widgets.common-test :as common]
            [cljs.core.async :refer [put! chan <! alts! timeout close!]]
            [om.core :as om :include-macros true]
            [dommy.core :as dommy :refer-macros [sel sel1]]
            [om-widgets.grid :as grid]
            [om-widgets.core :as w]))


(deftest data-page
  (testing "grid data-page non-empty without index or total-rows"
    (let [rows '(1 2 3 4 5 6)
          total-rows (count rows)
          page-size 2
          chan (chan)]
      (is (= '(1 2) (grid/data-page {:rows rows} 0 page-size chan)))
      (is (= '(3 4) (grid/data-page {:rows rows} 1 page-size chan)))
      (is (= '(5 6) (grid/data-page {:rows rows} 2 page-size chan))))))
