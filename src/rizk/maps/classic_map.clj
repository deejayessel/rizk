(ns rizk.maps.classic-map
  (:require [rizk.definitions :as definitions]))

(def classic-map-definition
  {"Alaska"
   {:name      "Alaska"
    :neighbors ["Northwest Territory"
                "Alberta"]
    :group    "North America"}

   "Northwest Territory"
   {:name      "Northwest Territory"
    :neighbors ["Alaska"
                "Greenland"
                "Alberta"
                "Ontario"]
    :group    "North America"}

   "Greenland"
   {:name      "Greenland"
    :neighbors ["Northwest Territory"
                "Ontario"
                "Quebec"]
    :group    "North America"}

   "Alberta"
   {:name      "Alberta"
    :neighbors ["Alaska"
                "Northwest Territory"
                "Ontario"
                "Western United States"]
    :group    "North America"}

   "Ontario"
   {:name      "Ontario"
    :neighbors ["Northwest Territory"
                "Alberta"
                "Greenland"
                "Quebec"
                "Western United States"
                "Eastern United States"]
    :group    "North America"}

   "Quebec"
   {:name      "Quebec"
    :neighbors ["Greenland"
                "Ontario"
                "Eastern United States"]
    :group    "North America"}

   "Western United States"
   {:name      "Western United States"
    :neighbors ["Alberta"
                "Ontario"
                "Eastern United States"
                "Central America"]
    :group    "North America"}

   "Eastern United States"
   {:name      "Eastern United States"
    :neighbors ["Quebec"
                "Ontario"
                "Western United States"
                "Central America"]
    :group    "North America"}

   "Central America"
   {:name      "Central America"
    :neighbors ["Eastern United States"
                "Western United States"]
    :group    "North America"}



   })

(definitions/add-definitions! classic-map-definition)

(keys classic-map-definition)