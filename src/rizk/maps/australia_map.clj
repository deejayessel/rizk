(ns rizk.maps.australia-map
  (:require [rizk.definitions :as definitions]))

(def australia-map-definition

  {"Indonesia"
   {:name        "Indonesia"
    :entity-type :tile
    :neighbors   ["New Guinea"
                  "Western Australia"]
    :group      "Australia"}

   "New Guinea"
   {:name        "New Guinea"
    :entity-type :tile
    :neighbors   ["Indonesia"
                  "Western Australia"
                  "Eastern Australia"]
    :group      "Australia"}

   "Western Australia"
   {:name        "Western Australia"
    :entity-type :tile
    :neighbors   ["Indonesia"
                  "New Guinea"
                  "Eastern Australia"]
    :group      "Australia"}

   "Eastern Australia"
   {:name        "Eastern Australia"
    :entity-type :tile
    :neighbors   ["New Guinea"
                  "Western Australia"]
    :group      "Australia"}

   "Australia"
   {:name         "Australia"
    :entity-type  :group
    :group-bonus 2
    :member-tiles ["Indonesia"
                   "New Guinea"
                   "Western Australia"
                   "Eastern Australia"]}})

(definitions/add-definitions! australia-map-definition)

(keys australia-map-definition)