(ns rizk.util
  (:require [ysera.test :refer [is is-not is= error?]]))

(defn floor
  "Returns the floor of n."
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
  "Returns the ceiling of n."
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
  "Checks whether a value is a non-negative integer."
  {:test (fn []
           (is (non-neg-int? 0))
           (is (non-neg-int? 1))
           (is-not (non-neg-int? -1)))}
  [n]
  (or (pos-int? n) (zero? n)))

(defn int-or-else
  "Return the value of the input n if n is an integer.
  Otherwise, return default."
  {:test (fn []
           (is= (int-or-else 0 nil)
                0)
           (is= (int-or-else 1 nil)
                1)
           (is= (int-or-else 7 0)
                7))}
  [n default]
  {:pre [(or (nil? n) (int? n))]}
  (if (nil? n)
    default
    n))