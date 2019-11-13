(ns rizk.maps.square-map
  (:require [rizk.definitions :as definitions]))

;; A simple cycle of 4 tiles
(def square-map-definition

  {"i"
   {:name        "i"
    :entity-type :tile
    :neighbors   ["ii" "iv"]
    :group      "square"}

   "ii"
   {:name        "ii"
    :entity-type :tile
    :neighbors   ["i" "iii"]
    :group      "square"}

   "iii"
   {:name        "iii"
    :entity-type :tile
    :neighbors   ["ii" "iv"]
    :group      "square"}

   "iv"
   {:name        "iv"
    :entity-type :tile
    :neighbors   ["iii" "i"]
    :group      "square"}

   "square"
   {:name         "square"
    :entity-type  :group
    :group-bonus 4
    :member-tiles ["i" "ii" "iii" "iv"]}})

(definitions/add-definitions! square-map-definition)

(keys square-map-definition)