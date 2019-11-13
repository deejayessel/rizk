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
                     (filter (fn [x] (or (= (:region x) "square")
                                         (= (:name x) "square"))))
                     (set))
                (set [{:name        "i"
                       :entity-type :node
                       :neighbors   ["ii" "iv"]
                       :region      "square"}

                      {:name        "ii"
                       :entity-type :node
                       :neighbors   ["i" "iii"]
                       :region      "square"}

                      {:name        "iii"
                       :entity-type :node
                       :neighbors   ["ii" "iv"]
                       :region      "square"}

                      {:name        "iv"
                       :entity-type :node
                       :neighbors   ["iii" "i"]
                       :region      "square"}

                      {:name         "square"
                       :entity-type  :region
                       :region-bonus 4
                       :member-nodes ["i" "ii" "iii" "iv"]}])))}
  []
  (vals (deref definitions-atom)))

(defn- get-definition
  {:test (fn []
           (is= (get-definition "iv")
                {:name        "iv"
                 :entity-type :node
                 :neighbors   ["iii" "i"]
                 :region      "square"})
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
           (is= (->> (get-entities-of-type :region)
                     (filter (fn [n] (= (:name n) "square")))
                     (map :name))
                ["square"])
           (is= (->> (get-entities-of-type :node)
                     (filter (fn [n] (= (:region n) "square")))
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

(defn get-region-defns
  {:test (fn []
           (is= (->> (get-region-defns)
                     (map :name))
                ["square"]))}
  []
  (get-entities-of-type :region))

(defn get-all-node-defns
  {:test (fn []
           (is= (->> (get-all-node-defns)
                     (map :name)
                     (set))
                (set ["i"
                      "ii"
                      "iii"
                      "iv"])))}
  []
  (get-entities-of-type :node))

(defn get-node-defn
  {:test (fn []
           (is= (get-node-defn "i")
                {:name        "i"
                 :entity-type :node
                 :neighbors   ["ii"
                               "iv"]
                 :region      "square"}))}
  [node-name]
  {:pre [(string? node-name)]}
  (get-definition node-name))

(defn get-region-defn
  "Returns the definition of a specified region. Throws an error if region is not found."
  {:test (fn []
           (is= (get-region-defn "square")
                {:name         "square"
                 :entity-type  :region
                 :region-bonus 4
                 :member-nodes ["i" "ii" "iii" "iv"]})
           (error? (get-region-defn "iii")))}
  [region-name]
  {:pre [(string? region-name)]}
  (let [region-def (get-definition region-name)]
    (if (or (empty? region-def) (not= (:entity-type region-def) :region))
      (error (str "Unable to find region: " region-name "."))
      region-def)))