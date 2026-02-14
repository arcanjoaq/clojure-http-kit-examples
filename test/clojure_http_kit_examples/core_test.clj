(ns clojure-http-kit-examples.core-test
  (:require
   [clojure.test :refer [deftest is testing]]))

(deftest first-test
  (testing "Sample"
    (is (= 1 1))))

(deftest second-test
  (testing "first"
    (is (= 1 (first [1 2 3 4 5]))))
  (testing "rest"
    (is (= [2 3 4 5] (rest [1 2 3 4 5])))))
