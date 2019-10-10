(ns rizk.maps.australia-map
  (:require [rizk.definitions :as definitions]))

(def australia-map-definition

  {"Indonesia"
   {:name      "Indonesia"
    :neighbors ["New Guinea"
                "Western Australia"]
    :region    "Australia"}

   "New Guinea"
   {:name      "New Guinea"
    :neighbors ["Indonesia"
                "Western Australia"
                "Eastern Australia"]
    :region    "Australia"}

   "Western Australia"
   {:name      "Western Australia"
    :neighbors ["Indonesia"
                "New Guinea"
                "Eastern Australia"]
    :region    "Australia"}

   "Eastern Australia"
   {:name      "Eastern Australia"
    :neighbors ["New Guinea"
                "Western Australia"]
    :region    "Australia"}})

(definitions/add-definitions! australia-map-definition)

(keys australia-map-definition)