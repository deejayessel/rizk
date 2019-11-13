(ns rizk.maps.square-map
  (:require [rizk.definitions :as definitions]))

;; A simple cycle of 4 nodes
(def square-map-definition

  {"i"
   {:name        "i"
    :entity-type :node
    :neighbors   ["ii" "iv"]
    :region      "square"}

   "ii"
   {:name        "ii"
    :entity-type :node
    :neighbors   ["i" "iii"]
    :region      "square"}

   "iii"
   {:name        "iii"
    :entity-type :node
    :neighbors   ["ii" "iv"]
    :region      "square"}

   "iv"
   {:name        "iv"
    :entity-type :node
    :neighbors   ["iii" "i"]
    :region      "square"}

   "square"
   {:name         "square"
    :entity-type  :region
    :region-bonus 4
    :member-nodes ["i" "ii" "iii" "iv"]}})

(definitions/add-definitions! square-map-definition)

(keys square-map-definition)