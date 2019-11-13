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
                     (filter (fn [x] (or (= (:group x) "square")
                                         (= (:name x) "square"))))
                     (set))
                (set [{:name        "i"
                       :entity-type :tile
                       :neighbors   ["ii" "iv"]
                       :group      "square"}

                      {:name        "ii"
                       :entity-type :tile
                       :neighbors   ["i" "iii"]
                       :group      "square"}

                      {:name        "iii"
                       :entity-type :tile
                       :neighbors   ["ii" "iv"]
                       :group      "square"}

                      {:name        "iv"
                       :entity-type :tile
                       :neighbors   ["iii" "i"]
                       :group      "square"}

                      {:name               "square"
                       :entity-type        :group
                       :group-bonus       4
                       :member-tiles ["i" "ii" "iii" "iv"]}])))}
  []
  (vals (deref definitions-atom)))

(defn- get-definition
  {:test (fn []
           (is= (get-definition "iv")
                {:name        "iv"
                 :entity-type :tile
                 :neighbors   ["iii" "i"]
                 :group      "square"})
           ; The name can be present in a map with :name as a key
           (is= (get-definition {:name "i"})
                (get-definition "i"))

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
           (is= (->> (get-entities-of-type :group)
                     (filter (fn [n] (= (:name n) "square")))
                     (map :name))
                ["square"])
           (is= (->> (get-entities-of-type :tile)
                     (filter (fn [n] (= (:group n) "square")))
                     (map :name)
                     (set))
                (set ["i"
                      "ii"
                      "iii"
                      "iv"])))}
  [entity-type]
  {:pre [(keyword? entity-type)]}
  (->> (get-all-definitions)
       (filter (fn [entity]
                 (= (:entity-type entity)
                    entity-type)))))

(defn get-group-defns
  {:test (fn []
           (is= (->> (get-group-defns)
                     (map :name))
                ["square"]))}
  []
  (get-entities-of-type :group))

(defn get-all-tile-defns
  {:test (fn []
           (is= (->> (get-all-tile-defns)
                     (map :name)
                     (set))
                (set ["i"
                      "ii"
                      "iii"
                      "iv"])))}
  []
  (get-entities-of-type :tile))

(defn get-tile-defn
  {:test (fn []
           (is= (get-tile-defn "i")
                {:name        "i"
                 :entity-type :tile
                 :neighbors   ["ii"
                               "iv"]
                 :group      "square"}))}
  [tile-name]
  {:pre [(string? tile-name)]}
  (get-definition tile-name))

(defn get-group-defn
  "Returns the definition of a specified group. Throws an error if group is not found."
  {:test (fn []
           (is= (get-group-defn "square")
                {:name               "square"
                 :entity-type        :group
                 :group-bonus       4
                 :member-tiles ["i" "ii" "iii" "iv"]})
           (error? (get-group-defn "iii")))}
  [group-name]
  {:pre [(string? group-name)]}
  (let [group-def (get-definition group-name)]
    (if (or (empty? group-def) (not= (:entity-type group-def) :group))
      (error (str "Unable to find group: " group-name "."))
      group-def)))