(ns rizk.util
  (:require [ysera.test :refer [is is-not is= error?]]))

(defn floor
  {:test (fn []
           (is= (floor (/ 22 7))
                3)
           (is= (floor (/ 20 7))
                2)
           (is= (floor (/ 21 7))
                3))}
  [n]
  (if (ratio? n)
    (let [num (numerator n)
          den (denominator n)]
      (quot num den))
    n))

(defn ceiling
  {:test (fn []
           (is= (ceiling (/ 22 7))
                4)
           (is= (ceiling (/ 20 7))
                3)
           (is= (ceiling (/ 21 7))
                3))}
  [n]
  (if (ratio? n)
    (let [num (numerator n)
          den (denominator n)]
      (+ (quot num den) 1))
    n))

(defn non-neg-int?
  {:test (fn []
           (is (non-neg-int? 0))
           (is (non-neg-int? 1))
           (is-not (non-neg-int? -1)))}
  [n]
  (or (pos-int? n) (zero? n)))