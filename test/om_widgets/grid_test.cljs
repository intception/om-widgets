(ns om-widgets.grid-test
  (:require-macros [cljs.test :refer (is deftest run-tests testing)])
  (:require [cljs.test :as t]
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
  (let [get-page (fn [rows current-page page-size]
                   (grid/data-page rows current-page page-size (chan) {} (atom false)))]
    (testing "grid data-page non-empty without index"
      (let [rows '(1 2 3 4 5 6) page-size 2]
        (is (= '(1 2) (get-page {:rows rows} 0 page-size)))
        (is (= '(3 4) (get-page {:rows rows} 1 page-size)))
        (is (= '(5 6) (get-page {:rows rows} 2 page-size)))))

    (testing "grid empty data-page"
      (let [rows '() page-size (count rows)]
        (is (= '() (get-page {:rows rows} 0 page-size)))))

    (testing "grid data-page non-empty with current-page out of bounds"
      (let [rows '(1 2 3 4 5 6) page-size 3]
        (is (= '() (get-page {:rows rows} 3 page-size)))))

    (testing "grid data-page non-empty with with full page size"
      (let [rows '(1 2 3 4 5 6) page-size (count rows)]
        (is (= '(1 2 3 4 5 6) (get-page {:rows rows} 0 page-size)))))

    (testing "grid data-page non-empty with index"
      (let [rows '(1 2 3 4 5 6) page-size 4]
        (is (= '(1 2 3 4) (get-page {:rows rows :index 0} 0 page-size)))
        (is (= '(5 6) (get-page {:rows rows :index 0} 1 page-size)))
        (is (= '(3 4 5 6) (get-page {:rows rows :index 2} 1 page-size)))))))


(deftest build-page-boundaries
  ;; usage: (build-page-boundaries {:current-page X :page-size X :total-items X})
  (testing "normal page info"
    ;; [*[1 2]* [3 4][5 6]]  / 1-2 of 6
    (is (= {:start 1 :end 2}
           (grid/build-page-boundaries {:current-page 1
                                        :page-size 2
                                        :total-items 6})))
    ;; [[1 2] *[3 4]* [5 6]] / 3-4 of 6
    (is (= {:start 3 :end 4}
           (grid/build-page-boundaries {:current-page 2
                                        :page-size 2
                                        :total-items 6})))
    ;; [[1 2] [3 4] *[5 6]*] / 5-6 of 6
    (is (= {:start 5 :end 6}
           (grid/build-page-boundaries {:current-page 3
                                        :page-size 2
                                        :total-items 6}))))
  (testing "without items"
    ;; []  / 0-0 of 0
    (is (= {:start 0 :end 0}
           (grid/build-page-boundaries {:current-page 1
                                        :page-size 1
                                        :total-items 0}))))
  (testing "1 item"
    ;; [*[1]*]  / 1-1 of 1
    (is (= {:start 1 :end 1}
           (grid/build-page-boundaries {:current-page 1
                                        :page-size 1
                                        :total-items 1}))))
  (testing "2 pages, and the last page with just 1 item"
    ;; [*[1 2 3 4]* [5]] / 1-4 of 5
    (is (= {:start 1 :end 4}
           (grid/build-page-boundaries {:current-page 1
                                        :page-size 4
                                        :total-items 5})))
    ;; [[1 2 3 4] *[5]*] / 5-5 of 5
    (is (= {:start 5 :end 5}
           (grid/build-page-boundaries {:current-page 2
                                        :page-size 4
                                        :total-items 5}))))
  (testing "2 pages, and the last page (rest) smallest than the current page size"
    ;; [*[1 2 3 4]* [5 6]] / 1-4 of 6
    (is (= {:start 1 :end 4}
           (grid/build-page-boundaries {:current-page 1
                                        :page-size 4
                                        :total-items 6})))

    ;; [[1 2 3 4] *[5 6]*] / 5-6 of 6
    (is (= {:start 5 :end 6}
           (grid/build-page-boundaries {:current-page 2
                                        :page-size 4
                                        :total-items 6})))))
