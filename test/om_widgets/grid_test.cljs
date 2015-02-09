(ns om-widgets.grid-test
  (:require-macros [cemerick.cljs.test :refer (is deftest with-test run-tests testing test-var)])
  (:require [cemerick.cljs.test :as t]
            [om-widgets.common-test :as common]
            [cljs.core.async :refer [put! chan <! alts! timeout close!]]
            [om.core :as om :include-macros true]
            [dommy.core :as dommy :refer-macros [sel sel1]]
            [om-widgets.grid :as grid]
            [om-widgets.core :as w]))


;; TODO:
;; add grid component tests
;; add grid pager tests
;; add custom row grid tests

(deftest data-page
  ;; data-page usage: (data-page source current-page page-size events-channel)
  (let [chan (chan)]

    (testing "grid data-page non-empty without index"
      (let [rows '(1 2 3 4 5 6) page-size 2]
        (is (= '(1 2) (grid/data-page {:rows rows} 0 page-size chan)))
        (is (= '(3 4) (grid/data-page {:rows rows} 1 page-size chan)))
        (is (= '(5 6) (grid/data-page {:rows rows} 2 page-size chan)))))

    (testing "grid empty data-page"
      (let [rows '() page-size (count rows)]
        (is (= '() (grid/data-page {:rows rows} 0 page-size chan)))))

    (testing "grid data-page non-empty with current-page out of bounds"
      (let [rows '(1 2 3 4 5 6) page-size 3]
        (is (= '() (grid/data-page {:rows rows} 3 page-size chan)))))

    (testing "grid data-page non-empty with with full page size"
      (let [rows '(1 2 3 4 5 6) page-size (count rows)]
        (is (= '(1 2 3 4 5 6) (grid/data-page {:rows rows} 0 page-size chan)))))

    (testing "grid data-page non-empty with index"
      (let [rows '(1 2 3 4 5 6) page-size 4]
        (is (= '(1 2 3 4) (grid/data-page {:rows rows :index 0} 0 page-size chan)))
        (is (= '(5 6) (grid/data-page {:rows rows :index 0} 1 page-size chan)))
        (is (= '(3 4 5 6) (grid/data-page {:rows rows :index 2} 1 page-size chan)))))))
