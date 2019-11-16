(ns rizk.test_all
  (:require [clojure.test :refer [run-tests successful?]]
            [ysera.test :refer [deftest is]]
            [rizk.definitions_loader]))

(deftest tests-all
         "Bootstrapping with the required namespaces, finds all the rizk.* namespaces (except this one),
                       requires them, and runs all their tests."
         (let [namespaces (->> (all-ns)
                               (map str)
                               (filter (fn [x] (re-matches #"rizk\..*" x)))
                               (remove (fn [x] (or (= "rizk.test_all" x)
                                                   (= "rizk.definitions_loader" x))))
                               (cons "rizk.definitions_loader")
                               (map symbol))]
           (is (successful? (time (apply run-tests namespaces))))))
