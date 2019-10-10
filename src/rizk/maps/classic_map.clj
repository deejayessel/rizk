(ns rizk.maps.classic-map)

(def classic-map-definition
  {"Alaska"
   {:name      "Alaska"
    :neighbors ["Northwest Territory"
                "Alberta"]
    :region    "North America"}

   "Northwest Territory"
   {:name      "Northwest Territory"
    :neighbors ["Alaska"
                "Greenland"
                "Alberta"
                "Ontario"]
    :region    "North America"}

   "Greenland"
   {:name      "Greenland"
    :neighbors ["Northwest Territory"
                "Ontario"
                "Quebec"]
    :region    "North America"}

   "Alberta"
   {:name      "Alberta"
    :neighbors ["Alaska"
                "Northwest Territory"
                "Ontario"
                "Western United States"]
    :region    "North America"}

   "Ontario"
   {:name      "Ontario"
    :neighbors ["Northwest Territory"
                "Alberta"
                "Greenland"
                "Quebec"
                "Western United States"
                "Eastern United States"]
    :region    "North America"}

   "Quebec"
   {:name      "Quebec"
    :neighbors ["Greenland"
                "Ontario"
                "Eastern United States"]
    :region    "North America"}

   "Western United States"
   {:name      "Western United States"
    :neighbors ["Alberta"
                "Ontario"
                "Eastern United States"
                "Central America"]
    :region    "North America"}

   "Eastern United States"
   {:name      "Eastern United States"
    :neighbors ["Quebec"
                "Ontario"
                "Western United States"
                "Central America"]
    :region    "North America"}

   "Central America"
   {:name      "Central America"
    :neighbors ["Eastern United States"
                "Western United States"]
    :region    "North America"}



   })

(definitions/add-definitions! classic-map-definition)

(keys classic-map-definition)