(ns rizk.construct
  (:require [ysera.test :refer [is= is is-not error?]]
            [ysera.error :refer [error]]
            [rizk.util :refer [int-or-else]]
            [rizk.definitions :refer [get-all-node-defns
                                      get-node-defn
                                      get-region-defn
                                      get-region-defns]]
            [rizk.random :refer [random-partition-with-seed]]
            [clojure.set :refer [difference]]))

(defn create-empty-state
  "Creates an empty state."
  {:test (fn []
           (is= (create-empty-state 2)
                {:player-in-turn             "p1"
                 :turn-phase                 :reinforcement-phase
                 :seed                       0
                 :players                    ["p1" "p2"]
                 :nodes                      {}
                 :initial-army-size          20
                 :initial-reinforcement-size 3}))}
  [num-players]
  {:pre [(int? num-players) (>= num-players 2)]}
  {:player-in-turn             "p1"
   :turn-phase                 :reinforcement-phase
   :seed                       0
   :players                    (->> (range 1 (inc num-players))
                                    (map (fn [n] (str "p" n))))
   :nodes                      {}
   :initial-army-size          20
   :initial-reinforcement-size 3})

(defn create-node
  "Creates a node without owner-id."
  {:test (fn []
           (is= (create-node "i")
                {:name        "i"
                 :troop-count 1})
           (is= (create-node "i" :troop-count 2)
                {:name        "i"
                 :troop-count 2})
           (error? (create-node "Williamstown")))}
  [node-name & kvs]
  (let [definition (get-node-defn node-name)
        {troop-count :troop-count} kvs
        node {:name        node-name
              :troop-count (int-or-else troop-count 1)}]
    (if (nil? definition)
      (error "Couldn't get definition of " node-name ". Are definitions loaded?")
      (if (empty? kvs)
        node
        (apply assoc node kvs)))))

(defn update-turn-phase
  "Updates the turn phase."
  {:test (fn []
           (is= (-> (create-empty-state 2)
                    (update-turn-phase :attack-phase)
                    (:turn-phase))
                :attack-phase)
           (is= (-> (create-empty-state 2)
                    (update-turn-phase (fn [_] :attack-phase))
                    (:turn-phase))
                :attack-phase))}
  [state fn-or-val]
  {:pre [(map? state) (or (fn? fn-or-val) (keyword? fn-or-val))]}
  (if (fn? fn-or-val)
    (update state :turn-phase fn-or-val)
    (assoc state :turn-phase fn-or-val)))

(defn active-player-id
  "Returns the id of the player in turn."
  {:test (fn []
           (is= (-> (create-empty-state 2)
                    (:player-in-turn))
                "p1"))}
  [state]
  (:player-in-turn state))

(defn players
  "Returns the list of player names."
  {:test (fn []
           (is= (->> (create-empty-state 3)
                     (players))
                ["p1" "p2" "p3"]))}
  [state]
  {:pre [(map? state)]}
  (:players state))

(defn player-count
  "Returns the number of players in the state."
  {:test (fn []
           (is= (-> (create-empty-state 3)
                    (player-count))
                3))}
  [state]
  {:pre [(map? state)]}
  (count (players state)))

(defn opponent-ids
  "Returns the ids of all opponents of the input player."
  {:test (fn []
           (is= (-> (create-empty-state 2)
                    (opponent-ids "p1"))
                ["p2"])
           (is= (-> (create-empty-state 9)
                    (opponent-ids "p5")
                    (sort))
                ["p1" "p2" "p3" "p4"
                 "p6" "p7" "p8" "p9"]))}
  [state player-id]
  {:pre [(map? state) (string? player-id)]}
  (->> (players state)
       (remove (fn [p] (= p player-id)))))

(defn neighbor-names
  "Returns the names of all neighbors of the node with the given name."
  {:test (fn []
           (is= (neighbor-names "i")
                ["ii" "iv"]))}
  [node-name]
  {:pre [(string? node-name)]}
  (let [node-defn (get-node-defn node-name)]
    (:neighbors node-defn)))

(defn containing-region-name
  "Returns the name of the region containing the input node."
  {:test (fn []
           (is= (containing-region-name "i")
                "square"))}
  [node-name]
  {:pre [(string? node-name)]}
  (let [node-defn (get-node-defn node-name)]
    (:region node-defn)))

(defn get-nodes
  "Returns all nodes in the state or, optionally,
  in a given player's possession."
  {:test (fn []
           (is= (-> (create-empty-state 2)
                    (get-nodes))
                [])
           (is= (-> (create-empty-state 2)
                    (get-nodes "p1"))
                []))}
  ([state]
   {:pre [(map? state)]}
   (-> (:nodes state)
       (vals)
       (vec)))
  ([state player-id]
   {:pre [(map? state) (string? player-id)]}
   (->> (get-nodes state)
        (filter (fn [node]
                  (= (:owner-id node
                       player-id)))))))

(defn add-node
  "Adds a node to the state."
  {:test (fn []
           (is= (as-> (create-empty-state 3) $
                      (add-node $ "p1" (create-node "i"))
                      (get-nodes $ "p1")
                      (map :name $))
                ["i"]))}
  [state player-id node]
  {:pre [(map? state) (string? player-id) (map? node)]}
  (assoc-in state [:nodes (:name node)]
            (assoc node :owner-id player-id)))

(defn get-node
  "Returns the node in the state identified by node-name."
  {:test (fn []
           (is= (-> (create-empty-state 2)
                    (add-node "p1" (create-node "i"))
                    (get-node "i"))
                {:name        "i"
                 :owner-id    "p1"
                 :troop-count 1}))}
  [state node-name]
  {:pre [(map? state) (string? node-name)]}
  (get-in state [:nodes node-name]))

(defn get-in-node
  "Returns the value associated with the key in the node."
  {:test (fn []
           (is= (-> (create-empty-state 2)
                    (add-node "p1" (create-node "i"))
                    (get-in-node "i" :owner-id))
                "p1"))}
  [state node-name key]
  {:pre [(map? state) (string? node-name) (keyword? key)]}
  (get-in state [:nodes node-name key]))

(defn add-nodes
  "Adds a collection of nodes to the state."
  {:test (fn []
           (let [state (-> (create-empty-state 3)
                           (add-nodes "p1" [(create-node "i")
                                            (create-node "ii")]))
                 nodes (get-nodes state)]
             (is= (map :owner-id nodes)
                  ["p1" "p1"])
             (is= (map :name nodes)
                  ["i" "ii"])))}
  [state owner-id nodes]
  {:pre [(map? state) (string? owner-id) (every? map? nodes)]}
  (reduce (fn [state node]
            (add-node state owner-id node))
          state
          nodes))

(defn replace-node
  "Adds new-node into the state, removing any other node that shares the same name."
  {:test (fn []
           (let [new-node (create-node "i"
                                       :troop-count 5
                                       :owner-id "p2")]
             (is= (-> (create-empty-state 2)
                      (add-node "p1" (create-node "i"))
                      (replace-node new-node)
                      (get-node "i"))
                  new-node)))}
  [state new-node]
  {:pre [(map? state) (map? new-node)]}
  (assoc-in state [:nodes (:name new-node)] new-node))

(defn replace-nodes
  "Replaces multiple nodes."
  {:test (fn []
           (let [new-nodes [(create-node "i"
                                         :troop-count 5
                                         :owner-id "p2")
                            (create-node "ii"
                                         :troop-count 7
                                         :owner-id "p2")]
                 state (-> (create-empty-state 2)
                           (add-nodes "p1" [(create-node "i")
                                            (create-node "ii")])
                           (replace-nodes new-nodes))]
             (is= (->> (get-nodes state)
                       (filter (fn [n] (or (= "i" (:name n))
                                           (= "ii" (:name n))))))
                  new-nodes)))}
  [state new-nodes]
  {:pre [(map? state) (coll? new-nodes) (every? map? new-nodes)]}
  (reduce (fn [state new-node]
            (replace-node state new-node))
          state
          new-nodes))

(defn update-node
  "Updates a node, given a key and either a function to apply to the current
  value, or a value to override to the current value with."
  {:test (fn []
           ; update owner
           (is= (-> (create-empty-state 2)
                    (add-node "p1" (create-node "i"))
                    (update-node "i" :owner-id "p2")
                    (get-node "i")
                    (:owner-id))
                "p2")
           ; update troop count
           (is= (-> (create-empty-state 2)
                    (add-node "p1" (create-node "i"))
                    (update-node "i" :troop-count 3)
                    (get-node "i")
                    (:troop-count))
                3)
           ; update with function
           (is= (-> (create-empty-state 2)
                    (add-node "p1" (create-node "i"))
                    (update-node "i" :troop-count inc)
                    (get-node "i")
                    (:troop-count))
                2))}
  [state node-name key fn-or-val]
  {:pre [(map? state) (string? node-name) (keyword? key) (or (fn? fn-or-val)
                                                             (pos-int? fn-or-val)
                                                             (string? fn-or-val))]}
  (let [node (get-node state node-name)]
    (replace-node state (if (fn? fn-or-val)
                          (update node key fn-or-val)
                          (assoc node key fn-or-val)))))

(defn randomly-assign-nodes
  "Randomly assigns nodes to the players in the game.
  Each assigned territory has 1 troop present.
  In the case that the number of players does not divide the number of
  nodes, no two players should have a node-count differing by more than 1."
  {:test (fn []
           ; Check that no two players have node-counts differing by more than 1
           (let [counts (->> (create-empty-state 3)
                             (randomly-assign-nodes)
                             (get-nodes)
                             (map :owner-id)
                             (frequencies)
                             (vals))]
             (is (<= (- (apply max counts)
                        (apply min counts))
                     1)))
           ; Check that all nodes are assigned
           (is (->> (create-empty-state 2)
                    (randomly-assign-nodes)
                    (get-nodes)
                    (every? (fn [t] (contains? t :owner-id)))
                    ))
           ; All nodes have 1 troop count
           (is (->> (create-empty-state 3)
                    (randomly-assign-nodes)
                    (get-nodes)
                    (every? (fn [t] (= (:troop-count t) 1)))))
           ; Randomly assign on subset of nodes
           (is= (->> (randomly-assign-nodes (create-empty-state 3)
                                            ["i" "ii"])
                     (get-nodes)
                     (map :name))
                ["i" "ii"]))}
  ([state]
   {:pre [(map? state)]}
   (->> (get-all-node-defns)
        (map :name)
        (randomly-assign-nodes state)))
  ([state node-names]
   {:pre [(map? state) (every? string? node-names)]}
   (let [seed (:seed state)
         player-count (player-count state)
         [seed node-name-partns] (random-partition-with-seed seed player-count node-names)
         state (assoc state :seed seed)                     ;update seed in state
         indexed-partns (map-indexed (fn [index part] {:player-id (str "p" (inc index))
                                                       :partition part})
                                     node-name-partns)]
     (reduce (fn [state {id         :player-id
                         node-names :partition}]
               (add-nodes state id (map create-node node-names)))
             state
             indexed-partns))))

(defn create-game
  "Creates a starting game state."
  {:test (fn []
           (is= (create-game 2 [{:nodes [(create-node "i" :troop-count 10)
                                         "ii"]}
                                {:nodes ["iii" "iv"]}]
                             :initial-army-size 30)
                {:player-in-turn             "p1"
                 :turn-phase                 :reinforcement-phase
                 :seed                       -9203025489357073502
                 :players                    ["p1" "p2"]
                 :nodes                      {"i"   {:name        "i"
                                                     :owner-id    "p1"
                                                     :troop-count 10}
                                              "ii"  {:name        "ii"
                                                     :owner-id    "p1"
                                                     :troop-count 1}
                                              "iii" {:name        "iii"
                                                     :owner-id    "p2"
                                                     :troop-count 1}
                                              "iv"  {:name        "iv"
                                                     :owner-id    "p2"
                                                     :troop-count 1}}
                 :initial-army-size          30
                 :initial-reinforcement-size 3}))}
  ([num-players]
   {:pre [(>= num-players 2)]}
   (-> (create-empty-state num-players)
       (randomly-assign-nodes)))
  ([num-players data & kvs]
   {:pre [(>= num-players 2) (vector? data)]}
   (let [players-data (map-indexed (fn [index player-data]
                                     (assoc player-data :player-id (str "p" (inc index))))
                                   data)
         state (as-> (create-game num-players) $
                     (reduce (fn [state {player-id :player-id
                                         nodes     :nodes}]
                               (let [nodes (map (fn [node]
                                                  (if (string? node)
                                                    (create-node node :owner-id player-id)
                                                    (assoc node :owner-id player-id)))
                                                nodes)]
                                 (replace-nodes state nodes)))
                             $
                             players-data))]
     (if (empty? kvs)
       state
       (apply assoc state kvs)))))

(defn neighbors?
  "Returns true if the two nodes are neighbors, false otherwise."
  {:test (fn []
           (is (neighbors? "i"
                           "ii"))
           (is-not (neighbors? "i"
                               "iii")))}
  [node-name-1 node-name-2]
  {:pre [(string? node-name-1) (string? node-name-2)]}
  (->> (neighbor-names node-name-1)
       (filter (fn [neighbor-name] (= neighbor-name
                                      node-name-2)))
       (first)
       (some?)))

(defn owns-region?
  "Checks if a player owns a region."
  {:test (fn []
           (is-not (-> (create-game 2)
                       (owns-region? "p1" "square")))
           (is (-> (create-game 2 [{:nodes ["i" "ii" "iii" "iv"]}])
                   (owns-region? "p1" "square"))))}
  [state player-id region-name]
  {:pre [(map? state) (string? player-id) (string? region-name)]}
  (->> (get-region-defn region-name)
       (:member-nodes)
       (map (fn [name] (get-node state name)))
       (map :owner-id)
       (filter (fn [owner-id] (not= owner-id
                                    player-id)))
       (empty?)))

(defn get-owned-regions
  "Returns the regions owned by the player."
  {:test (fn []
           (is= (-> (create-game 2)
                    (get-owned-regions "p1"))
                [])
           (is= (-> (create-game 2 [{:nodes ["i" "ii" "iii" "iv"]}])
                    (get-owned-regions "p1"))
                ["square"]))}
  [state player-id]
  {:pre [(map? state) (string? player-id)]}
  (let [region-names (map :name (get-region-defns))]
    (filter (fn [region-name]
              (owns-region? state player-id region-name))
            region-names)))