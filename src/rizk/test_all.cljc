(ns rizk.test_all
  (:require [clojure.test :refer [run-tests successful?]]
            [ysera.test :refer [deftest is]]
            [rizk.random]
            [rizk.util]
            [rizk.core]
            [rizk.construct]
            [rizk.definitions-loader]
            [rizk.definitions]))

(deftest test-all
         "Bootstrapping with the required namespaces, finds all the firestone.* namespaces (except this one),
                requires them, and runs all their tests."
         (let [namespaces (->> (all-ns)
                               (map str)
                               (filter (fn [x] (re-matches #"rizk\..*" x)))
                               (remove (fn [x] (= "rizk.test_all" x)))
                               (map symbol))]
           (is (successful? (time (apply run-tests namespaces))))))