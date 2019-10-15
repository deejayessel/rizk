(ns rizk.definitions
  (:require [ysera.test :refer [is is= is-not error?]]
            [ysera.error :refer [error]]))

; Here is where the definitions are stored
(defonce definitions-atom (atom {}))

(defn add-definitions!
  "Adds the given definitions to the game."
  [definitions]
  (swap! definitions-atom merge definitions))

(defn- get-all-definitions
  "Returns all definitions in the game."
  {:test (fn []
           (is= (->> (get-all-definitions)
                     (filter (fn [x] (or (= (:region x) "Australia")
                                         (= (:name x) "Australia"))))
                     (set))
                (set [{:name        "Indonesia"
                       :entity-type :tile
                       :neighbors   ["New Guinea"
                                     "Western Australia"]
                       :region      "Australia"}

                      {:name        "New Guinea"
                       :entity-type :tile
                       :neighbors   ["Indonesia"
                                     "Western Australia"
                                     "Eastern Australia"]
                       :region      "Australia"}

                      {:name        "Western Australia"
                       :entity-type :tile
                       :neighbors   ["Indonesia"
                                     "New Guinea"
                                     "Eastern Australia"]
                       :region      "Australia"}

                      {:name        "Eastern Australia"
                       :entity-type :tile
                       :neighbors   ["New Guinea"
                                     "Western Australia"]
                       :region      "Australia"}

                      {:name         "Australia"
                       :entity-type  :region
                       :region-bonus 2
                       :tiles        ["Indonesia"
                                      "New Guinea"
                                      "Western Australia"
                                      "Eastern Australia"]}])))}
  []
  (vals (deref definitions-atom)))

(defn- get-definition
  {:test (fn []
           (is= (get-definition "Eastern Australia")
                {:name        "Eastern Australia"
                 :entity-type :tile
                 :neighbors   ["New Guinea"
                               "Western Australia"]
                 :region      "Australia"})
           ; The name can be present in a map with :name as a key
           (is= (get-definition {:name "Indonesia"})
                (get-definition "Indonesia"))

           (error? (get-definition "Something that does not exist")))}
  [name-or-entity]
  {:pre [(or (string? name-or-entity)
             (and (map? name-or-entity)
                  (contains? name-or-entity :name)))]}
  (let [name (if (string? name-or-entity)
               name-or-entity
               (:name name-or-entity))
        definitions (deref definitions-atom)
        definition (get definitions name)]
    (when-not definition
      (error (str "The name " name-or-entity " does not exist. Are the definitions loaded?")))
    definition))

(defn- get-entities-of-type
  {:test (fn []
           (is= (->> (get-entities-of-type :region)
                     (map :name))
                ["Australia"])
           (is= (->> (get-entities-of-type :tile)
                     (map :name)
                     (set))
                (set ["Indonesia"
                      "New Guinea"
                      "Western Australia"
                      "Eastern Australia"])))}
  [entity-type]
  {:pre [(keyword? entity-type)]}
  (->> (get-all-definitions)
       (filter (fn [entity]
                 (= (:entity-type entity)
                    entity-type)))))

(defn get-region-defns
  {:test (fn []
           (is= (->> (get-region-defns)
                     (map :name))
                ["Australia"]))}
  []
  (get-entities-of-type :region))

(defn get-all-tile-defns
  {:test (fn []
           (is= (->> (get-all-tile-defns)
                     (map :name)
                     (set))
                (set ["Indonesia"
                      "New Guinea"
                      "Western Australia"
                      "Eastern Australia"])))}
  []
  (get-entities-of-type :tile))

(defn get-tile-defn
  {:test (fn []
           (is= (get-tile-defn "Eastern Australia")
                {:name        "Eastern Australia"
                 :entity-type :tile
                 :neighbors   ["New Guinea"
                               "Western Australia"]
                 :region      "Australia"}))}
  [tile-name]
  {:pre [(string? tile-name)]}
  (get-definition tile-name))