(ns rizk.maps.australia-map
  (:require [rizk.definitions :as definitions]))

(def australia-map-definition

  {"Indonesia"
   {:name        "Indonesia"
    :entity-type :node
    :neighbors   ["New Guinea"
                  "Western Australia"]
    :region      "Australia"}

   "New Guinea"
   {:name        "New Guinea"
    :entity-type :node
    :neighbors   ["Indonesia"
                  "Western Australia"
                  "Eastern Australia"]
    :region      "Australia"}

   "Western Australia"
   {:name        "Western Australia"
    :entity-type :node
    :neighbors   ["Indonesia"
                  "New Guinea"
                  "Eastern Australia"]
    :region      "Australia"}

   "Eastern Australia"
   {:name        "Eastern Australia"
    :entity-type :node
    :neighbors   ["New Guinea"
                  "Western Australia"]
    :region      "Australia"}

   "Australia"
   {:name         "Australia"
    :entity-type  :region
    :region-bonus 2
    :member-nodes ["Indonesia"
                   "New Guinea"
                   "Western Australia"
                   "Eastern Australia"]}})

(definitions/add-definitions! australia-map-definition)

(keys australia-map-definition)