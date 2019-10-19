(ns rizk.construct
  (:require [ysera.test :refer [is= is is-not error?]]
            [ysera.error :refer [error]]
            [rizk.util :refer [int-or-else]]
            [rizk.definitions :refer [get-all-tile-defns
                                      get-tile-defn
                                      get-region-defn
                                      get-region-defns]]
            [rizk.random :refer [random-partition-with-seed]]
            [clojure.set :refer [difference]]))

(defn create-empty-state
  "Creates an empty state."
  ; TODO player names?
  {:test (fn []
           (is= (create-empty-state 2)
                {:player-in-turn             1
                 :turn-phase                 :card-exchange-phase
                 :seed                       0
                 :players                    {1 {:id    1
                                                 :cards {:a 0
                                                         :b 0
                                                         :c 0}}
                                              2 {:id    2
                                                 :cards {:a 0
                                                         :b 0
                                                         :c 0}}}
                 :tiles                      {}
                 :initial-army-size          20
                 :initial-reinforcement-size 5
                 :initial-card-exchange-rate 4}))}
  [num-players]
  {:pre [(int? num-players) (>= num-players 2)]}
  {:player-in-turn             1
   :turn-phase                 :card-exchange-phase
   :seed                       0
   :players                    (->> (range 1 (inc num-players))
                                    (map (fn [player-num]
                                           {:id    player-num
                                            :cards {:a 0 :b 0 :c 0}}))
                                    (reduce (fn [a v]
                                              (assoc a (:id v) v))
                                            {}))
   :tiles                      {}
   :initial-army-size          20
   :initial-reinforcement-size 5
   :initial-card-exchange-rate 4})

(defn get-player-id-in-turn
  "Returns the id of the player in turn."
  {:test (fn []
           (is= (-> (create-empty-state 2)
                    (:player-in-turn))
                1))}
  [state]
  (:player-in-turn state))

(defn get-players
  "Returns the players in the state."
  {:test (fn []
           (is= (->> (create-empty-state 3)
                     (get-players)
                     (map :id))
                [1 2 3]))}
  [state]
  {:pre [(map? state)]}
  (-> (:players state)
      (vals)))

(defn player-count
  "Returns the number of players in the state."
  {:test (fn []
           (is= (-> (create-empty-state 3)
                    (player-count))
                3))}
  [state]
  {:pre [(map? state)]}
  (let [players (get-players state)]
    (count players)))

(defn get-player
  "Returns the player whose id matches the input id."
  {:test (fn []
           (is= (-> (create-empty-state 3)
                    (get-player 2))
                {:id    2
                 :cards {:a 0
                         :b 0
                         :c 0}}))}
  [state player-id]
  {:pre [(pos-int? player-id) (<= 1 player-id (player-count state))]}
  (get-in state [:players player-id]))

(defn get-opponent-ids
  "Returns the ids of all opponents of the input player."
  {:test (fn []
           (is= (-> (create-empty-state 2)
                    (get-opponent-ids 1))
                [2])
           (is= (-> (create-empty-state 10)
                    (get-opponent-ids 5)
                    (sort))
                [1 2 3 4 6 7 8 9 10]))}
  [state player-id]
  {:pre [(map? state)]}
  (->> (get-players state)
       (remove (fn [player] (= (:id player) player-id)))
       (map :id)))

(defn get-cards
  "Returns the player's hand."
  {:test (fn []
           (is= (-> (create-empty-state 3)
                    (get-cards 1))
                {:a 0
                 :b 0
                 :c 0}))}
  ([state player-id]
   {:pre [(map? state)]}
   (get-in state [:players player-id :cards])))

(defn create-tile
  "Creates a tile without owner-id."
  {:test (fn []
           (is= (create-tile "Indonesia")
                {:name        "Indonesia"
                 :troop-count 1})
           (is= (create-tile "Indonesia" :troop-count 2)
                {:name        "Indonesia"
                 :troop-count 2})
           (error? (create-tile "Williamstown")))}
  [tile-name & kvs]
  (let [definition (get-tile-defn tile-name)
        {troop-count :troop-count} kvs
        tile {:name        tile-name
              :troop-count (int-or-else troop-count 1)}]
    (if (nil? definition)
      (error "Couldn't get definition of " tile-name ". Are definitions loaded?")
      (if (empty? kvs)
        tile
        (apply assoc tile kvs)))))

(defn get-neighbor-names
  "Returns the names of all neighbors of the tile with the given name."
  {:test (fn []
           (is= (get-neighbor-names "Indonesia")
                ["New Guinea"
                 "Western Australia"]))}
  [tile-name]
  {:pre [(string? tile-name)]}
  (let [tile-defn (get-tile-defn tile-name)]
    (:neighbors tile-defn)))

(defn get-region-name
  "Returns the name of the region containing the input tile."
  {:test (fn []
           (is= (get-region-name "Western Australia")
                "Australia"))}
  [tile-name]
  {:pre [(string? tile-name)]}
  (let [tile-defn (get-tile-defn tile-name)]
    (:region tile-defn)))

(defn get-tiles
  "Returns all tiles in the state or, optionally,
  in a given player's possession."
  {:test (fn []
           (is= (-> (create-empty-state 2)
                    (get-tiles))
                [])
           (is= (-> (create-empty-state 2)
                    (get-tiles 1))
                []))}
  ([state]
   {:pre [(map? state)]}
   (-> (:tiles state)
       (vals)
       (vec)))
  ([state player-id]
   {:pre [(map? state)]}
   (->> (get-tiles state)
        (filter (fn [tile]
                  (= (:owner-id tile
                       player-id)))))))

(defn add-tile
  "Adds a tile to the state.

  If no troop-count given, defaults to 1."
  {:test (fn []
           (is= (as-> (create-empty-state 3) $
                      (add-tile $ 1 "Indonesia" 1)
                      (get-tiles $ 1)
                      (map :name $))
                ["Indonesia"]))}
  ([state owner-id tile-name]
   (add-tile state owner-id tile-name 1))
  ([state owner-id tile-name troop-count]
   {:pre [(map? state) (string? tile-name) (pos-int? troop-count)]}
   (assoc-in state
             [:tiles tile-name]
             {:name        tile-name
              :owner-id    owner-id
              :troop-count troop-count})))

(defn get-tile
  "Returns the tile in the state identified by tile-name."
  {:test (fn []
           (is= (-> (create-empty-state 2)
                    (add-tile 1 "Indonesia" 1)
                    (get-tile "Indonesia"))
                {:name        "Indonesia"
                 :owner-id    1
                 :troop-count 1}))}
  [state tile-name]
  {:pre [(map? state) (string? tile-name)]}
  (get-in state [:tiles tile-name]))

(defn add-tiles
  "Adds a collection of tiles to the state, given their names.
  Gives each tile a troop count of 1 by default."
  {:test (fn []
           (let [state (-> (create-empty-state 3)
                           (add-tiles 1 ["Indonesia" "New Guinea"] 1))
                 tiles (get-tiles state)]
             (is= (map :owner-id tiles)
                  [1 1])
             (is= (map :name tiles)
                  ["Indonesia" "New Guinea"])))}
  ([state owner-id tile-names]
   (add-tiles state owner-id tile-names 1))
  ([state owner-id tile-names troop-count]
   {:pre [(map? state) (every? string? tile-names) (pos-int? troop-count)]}
   (reduce (fn [state tile-name]
             (add-tile state owner-id tile-name troop-count))
           state
           tile-names)))

(defn remove-tile
  "Removes a tile from the state."
  {:test (fn []
           (is= (-> (create-empty-state 2)
                    (add-tile 1 "Indonesia")
                    (remove-tile "Indonesia")
                    (get-tiles))
                [])
           (is= (-> (create-empty-state 2)
                    (remove-tile "Indonesia")
                    (get-tiles))
                [])
           (is= (as-> (create-empty-state 2) $
                      (add-tile $ 1 "New Guinea")
                      (remove-tile $ "Indonesia")
                      (get-tiles $)
                      (map :name $))
                ["New Guinea"]))}
  ([state tile-name]
   {:pre [(map? state) (string? tile-name)]}
   (update state :tiles (fn [tiles]
                          (dissoc tiles tile-name)))))

(defn replace-tile
  "Adds new-tile into the state, removing any other tile that shares the same name."
  {:test (fn []
           (let [new-tile (create-tile "Indonesia"
                                       :troop-count 5
                                       :owner-id 2)]
             (is= (-> (create-empty-state 2)
                      (add-tile 1 "Indonesia")
                      (replace-tile new-tile)
                      (get-tile "Indonesia"))
                  new-tile)))}
  [state new-tile]
  {:pre [(map? state) (map? new-tile)]}
  (assoc-in state
            [:tiles (:name new-tile)]
            new-tile))

(defn replace-tiles
  "Replaces multiple tiles."
  {:test (fn []
           (let [new-tiles [(create-tile "New Guinea"
                                         :troop-count 7
                                         :owner-id 2)
                            (create-tile "Indonesia"
                                         :troop-count 5
                                         :owner-id 2)]
                 state (-> (create-empty-state 2)
                           (add-tiles 1 ["New Guinea" "Indonesia"])
                           (replace-tiles new-tiles))]
             (is= (->> (get-tiles state)
                       (filter (fn [tile] (or (= (:name tile) "New Guinea")
                                              (= (:name tile) "Indonesia")))))
                  new-tiles)))}
  [state new-tiles]
  {:pre [(map? state) (coll? new-tiles) (every? map? new-tiles)]}
  (reduce (fn [state new-tile]
            (replace-tile state new-tile))
          state
          new-tiles))

(defn update-tile
  "Updates a tile, given a key and either a function to apply to the current
  value, or a value to override to the current value with."
  {:test (fn []
           ; update owner
           (is= (-> (create-empty-state 2)
                    (add-tile 1 "Indonesia")
                    (update-tile "Indonesia" :owner-id 2)
                    (get-tile "Indonesia")
                    (:owner-id))
                2)
           ; update troop count
           (is= (-> (create-empty-state 2)
                    (add-tile 1 "Indonesia")
                    (update-tile "Indonesia" :troop-count 3)
                    (get-tile "Indonesia")
                    (:troop-count))
                3)
           ; update with function
           (is= (-> (create-empty-state 2)
                    (add-tile 1 "Indonesia")
                    (update-tile "Indonesia" :troop-count inc)
                    (get-tile "Indonesia")
                    (:troop-count))
                2))}
  [state tile-name key fn-or-val]
  {:pre [(map? state) (string? tile-name) (or (fn? fn-or-val)
                                              (pos-int? fn-or-val))]}
  (let [tile (get-tile state tile-name)]
    (replace-tile state (if (fn? fn-or-val)
                          (update tile key fn-or-val)
                          (assoc tile key fn-or-val)))))

(defn add-card
  "Adds a card to the specified player's hand."
  {:test (fn []
           (is= (-> (create-empty-state 3)
                    (add-card 1 :a)
                    (get-cards 1)
                    (:a))
                1))}
  [state player-id card-type]
  {:pre [(map? state) (keyword? card-type)]}
  (update-in state [:players player-id :cards card-type] inc))

(defn- update-cards
  "Adds or removes cards from the specified player's hand.
  Guarantees that no player ends up with a negative number of cards."
  {:test (fn []
           (is= (-> (create-empty-state 3)
                    (update-cards 1 {:a 1 :b 2})
                    (update-cards 1 {:a 2 :b 0})
                    (get-cards 1))
                {:a 3 :b 2 :c 0})
           (is= (-> (create-empty-state 3)
                    (update-cards 1 {:a 1 :b 2})
                    (update-cards 1 {:a -2 :b 1})
                    (get-cards 1))
                {:a 0 :b 3 :c 0}))}
  [state player-id cards]
  {:pre [(map? state) (or (nil? cards) (map? cards))]}
  (let [update-fn (fn [state card-type quantity]
                    (if (contains? cards card-type)
                      (update-in state
                                 [:players player-id :cards card-type]
                                 (fn [x] (max 0 (+ x quantity))))
                      state))]
    (reduce-kv update-fn state cards)))

(defn add-cards
  "Adds cards to a player's hand."
  {:test (fn []
           (is= (-> (create-empty-state 3)
                    (add-cards 1 {:a 1 :b 2})
                    (add-cards 1 {:a 2 :b 0})
                    (get-cards 1))
                {:a 3 :b 2 :c 0}))}
  [state player-id cards]
  {:pre [(map? state) (or (nil? cards) (map? cards))]}
  (update-cards state player-id cards))

(defn remove-card
  "Removes a card from the player's hand."
  {:test (fn []
           (is= (-> (create-empty-state 3)
                    (add-card 1 :a)
                    (remove-card 1 :a)
                    (get-cards 1)
                    (vals))
                [0 0 0])
           (is= (-> (create-empty-state 3)
                    (add-card 1 :a)
                    (add-card 1 :a)
                    (remove-card 1 :a)
                    (get-cards 1)
                    (vals))
                [1 0 0]))}
  [state player-id card-type]
  {:pre [(map? state) (keyword? card-type)]}
  (update-in state [:players player-id :cards card-type] dec))

(defn remove-cards
  "Removes multiple cards from the player's hand."
  {:test (fn []
           (is= (-> (create-empty-state 3)
                    (add-cards 1 {:a 1 :b 2})
                    (remove-cards 1 {:a 1 :b 1})
                    (get-cards 1))
                {:a 0 :b 1 :c 0})
           (is= (-> (create-empty-state 3)
                    (add-cards 1 {:a 1})
                    (remove-cards 1 {:b 1})
                    (get-cards 1))
                {:a 1 :b 0 :c 0}))}
  [state player-id cards]
  (let [neg-cards (reduce (fn [cards card-type] (update cards card-type -))
                          cards
                          (keys cards))]
    (update-cards state player-id neg-cards)))

(defn randomly-assign-tiles
  "Randomly assigns tiles to the players in the game.
  Each assigned territory has 1 troop present.
  In the case that the number of players does not divide the number of
  tiles, no two players should have a tile-count differing by more than 1."
  {:test (fn []
           ; Check that no two players have tile-counts differing by more than 1
           (let [counts (->> (create-empty-state 3)
                             (randomly-assign-tiles)
                             (get-tiles)
                             (map :owner-id)
                             (frequencies)
                             (vals))]
             (is (<= (- (apply max counts)
                        (apply min counts))
                     1)))
           ; Check that all tiles are assigned
           (is (->> (create-empty-state 2)
                    (randomly-assign-tiles)
                    (get-tiles)
                    (every? (fn [t] (contains? t :owner-id)))
                    ))
           ; All tiles have 1 troop count
           (is (->> (create-empty-state 3)
                    (randomly-assign-tiles)
                    (get-tiles)
                    (every? (fn [t] (= (:troop-count t) 1)))))
           ; Randomly assign on subset of tiles
           (is= (->> (randomly-assign-tiles (create-empty-state 3)
                                            ["Australia" "New Guinea"])
                     (get-tiles)
                     (map :name))
                ["Australia" "New Guinea"]))}
  ([state]
   {:pre [(map? state)]}
   (randomly-assign-tiles state (map :name (get-all-tile-defns))))
  ([state tile-names]
   {:pre [(map? state) (every? string? tile-names)]}
   (let [seed (:seed state)
         player-count (player-count state)
         [seed tile-name-partns] (random-partition-with-seed seed player-count tile-names)
         state (assoc state :seed seed)                     ;update seed in state
         indexed-partns (map-indexed (fn [index part] {:player-id (inc index)
                                                       :partition part})
                                     tile-name-partns)]
     (reduce (fn [state {id         :player-id
                         tile-names :partition}]
               (add-tiles state id tile-names))
             state
             indexed-partns))))

(defn create-game
  "Creates a starting game state."
  {:test (fn []
           (is= (create-game 2 [{:tiles [(create-tile "Indonesia" :troop-count 10)
                                         "New Guinea"]
                                 :cards {:a 1 :b 2 :c 0}}
                                {:tiles ["Eastern Australia" "Western Australia"]}]
                             :initial-army-size 30)
                {:player-in-turn             1
                 :turn-phase                 :card-exchange-phase
                 :seed                       -9203025489357073502
                 :players                    {1 {:id    1
                                                 :cards {:a 1
                                                         :b 2
                                                         :c 0}}
                                              2 {:id    2
                                                 :cards {:a 0
                                                         :b 0
                                                         :c 0}}}
                 :tiles                      {"Indonesia"         {:name        "Indonesia"
                                                                   :owner-id    1
                                                                   :troop-count 10}
                                              "New Guinea"        {:name        "New Guinea"
                                                                   :owner-id    1
                                                                   :troop-count 1}
                                              "Eastern Australia" {:name        "Eastern Australia"
                                                                   :owner-id    2
                                                                   :troop-count 1}
                                              "Western Australia" {:name        "Western Australia"
                                                                   :owner-id    2
                                                                   :troop-count 1}}
                 :initial-army-size          30
                 :initial-reinforcement-size 5
                 :initial-card-exchange-rate 4}))}
  ([num-players]
   {:pre [(>= num-players 2)]}
   (-> (create-empty-state num-players)
       (randomly-assign-tiles)))
  ([num-players data & kvs]
   {:pre [(>= num-players 2) (vector? data)]}
   (let [players-data (map-indexed (fn [index player-data]
                                     (assoc player-data :player-id (inc index)))
                                   data)
         state (as-> (create-game num-players) $
                     (reduce (fn [state {player-id :player-id
                                         tiles     :tiles
                                         cards     :cards}]
                               (let [tiles (map (fn [tile]
                                                  (if (string? tile)
                                                    (create-tile tile :owner-id player-id)
                                                    (assoc tile :owner-id player-id)))
                                                tiles)]
                                 (-> (replace-tiles state tiles)
                                     (add-cards player-id cards))))
                             $
                             players-data))]
     (if (empty? kvs)
       state
       (apply assoc state kvs)))))

;; TODO make test not definition/map dependent.

(defn get-tiles-from-names
  "Given a tile name, returns the corresponding tile from the state."
  {:test (fn []
           (is= (-> (create-game 2 [{:tiles ["Indonesia"
                                             "Western Australia"]}])
                    (get-tiles-from-names ["Indonesia"
                                           "Western Australia"]))
                [{:name        "Indonesia"
                  :owner-id    1
                  :troop-count 1}
                 {:name        "Western Australia"
                  :owner-id    1
                  :troop-count 1}]))}
  [state names]
  {:pre [(map? state) (vector? names) (every? string? names)]}
  (map (fn [tile-name] (get-tile state tile-name))
       names))

(defn neighbors?
  "Returns true if the two tiles are neighbors, false otherwise."
  {:test (fn []
           (is (neighbors? "Indonesia"
                           "New Guinea"))
           (is-not (neighbors? "Indonesia"
                               "Eastern Australia")))}
  [tile-name-1 tile-name-2]
  {:pre [(string? tile-name-1) (string? tile-name-2)]}
  (->> (get-neighbor-names tile-name-1)
       (filter (fn [neighbor-name] (= neighbor-name
                                      tile-name-2)))
       (first)
       (some?)))

(defn owns-region?
  "Checks if a player owns a region."
  {:test (fn []
           (not (-> (create-game 2)
                    (owns-region? 1 "Australia")))
           (is (-> (create-game 2 [{:tiles ["Indonesia"
                                            "Western Australia"
                                            "New Guinea"
                                            "Eastern Australia"]}])
                   (owns-region? 1 "Australia"))))}
  [state player-id region-name]
  (->> (get-region-defn region-name)
       (:tiles)
       (get-tiles-from-names state)
       (map :owner-id)
       (filter (fn [owner-id] (not= owner-id
                                    player-id)))
       (empty?)))

(defn get-owned-regions
  "Returns the regions owned by the player."
  {:test (fn []
           (is= (-> (create-game 2)
                    (get-owned-regions 1))
                ())
           (is= (-> (create-empty-state 2)
                    (add-tiles 1 ["Indonesia" "Western Australia" "New Guinea" "Eastern Australia"])
                    (get-owned-regions 1))
                ["Australia"]))}
  [state player-id]
  (let [region-names (map :name (get-region-defns))]
    (filter (fn [region-name]
              (owns-region? state player-id region-name))
            region-names)))