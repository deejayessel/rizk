(ns rizk.construct
  (:require [ysera.test :refer [is= is is-not error?]]
            [ysera.error :refer [error]]
            [rizk.definitions :refer [get-all-tile-defns
                                      get-tile-defn
                                      get-region-defn
                                      get-region-defns]]
            [rizk.random :refer [random-partition-with-seed]]
            [clojure.set :refer [difference]]))

(defn create-empty-state
  ; TODO player names?
  {:test (fn []
           (is= (create-empty-state 2)
                {:player-in-turn 1
                 :turn-phase     :card-exchange-phase
                 :seed           0
                 :players        {1 {:id    1
                                     :tiles []
                                     :cards {:a 0
                                             :b 0
                                             :c 0}}
                                  2 {:id    2
                                     :tiles []
                                     :cards {:a 0
                                             :b 0
                                             :c 0}}}
                 :rules          {:initial-army-size          20
                                  :initial-reinforcement-size 5
                                  :initial-card-exchange-rate 4}}))}
  [num-players]
  {:pre [(int? num-players) (>= num-players 2)]}
  {:player-in-turn 1
   :turn-phase     :card-exchange-phase
   :seed           0
   :players        (->> (range 1 (inc num-players))
                        (map (fn [player-num]
                               {:id    player-num
                                :tiles []
                                :cards {:a 0 :b 0 :c 0}}))
                        (reduce (fn [a v]
                                  (assoc a (:id v) v))
                                {}))
   :rules          {:initial-army-size          20
                    :initial-reinforcement-size 5
                    :initial-card-exchange-rate 4}})

(defn get-player-id-in-turn
  {:test (fn []
           (is= (-> (create-empty-state 2)
                    (:player-in-turn))
                1))}
  [state]
  (:player-in-turn state))

(defn get-players
  {:test (fn []
           (is= (->> (create-empty-state 3)
                     (get-players)
                     (map :id))
                [1 2 3]))}
  [state]
  {:pre [(map? state)]}
  (-> (:players state)
      (vals)))

(defn get-opponents-ids
  {:test (fn []
           (is= (-> (create-empty-state 2)
                    (get-opponents-ids 1))
                [2]))}
  [state player-id]
  {:pre [(map? state) ]}
  (->> (get-players state)
       (remove (fn [player] (= (:id player) player-id)))
       (map :id)))

(defn get-player-count
  {:test (fn []
           (is= (-> (create-empty-state 3)
                    (get-player-count))
                3))}
  [state]
  {:pre [(map? state)]}
  (let [players (get-players state)]
    (count players)))

(defn get-player
  {:test (fn []
           (is= (-> (create-empty-state 3)
                    (get-player 2))
                {:id    2
                 :tiles []
                 :cards {:a 0
                         :b 0
                         :c 0}}))}
  [state player-id]
  {:pre [(pos-int? player-id) (<= 1 player-id (get-player-count state))]}
  (get-in state [:players player-id]))

(defn get-rule
  {:test (fn []
           (is= (-> (create-empty-state 3)
                    (get-rule :initial-army-size))
                20))}
  [state key]
  {:pre [(map? state) (keyword? key)]}
  (get-in state [:rules key]))

(defn get-neighbor-names
  {:test (fn []
           (is= (get-neighbor-names "Indonesia")
                ["New Guinea"
                 "Western Australia"]))}
  [tile-name]
  {:pre [(string? tile-name)]}
  (let [tile-defn (get-tile-defn tile-name)]
    (:neighbors tile-defn)))

(defn neighbors?
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

(defn get-region
  {:test (fn []
           (is= (get-region "Western Australia")
                "Australia"))}
  [tile-name]
  {:pre [(string? tile-name)]}
  (let [tile-defn (get-tile-defn tile-name)]
    (:region tile-defn)))

(defn get-tiles
  {:test (fn []
           (is= (-> (create-empty-state 2)
                    (get-tiles))
                [])
           (is= (-> (create-empty-state 2)
                    (get-tiles 1))
                []))}
  ([state]
   {:pre [(map? state)]}
   (->> (:players state)
        (vals)
        (map :tiles)
        (apply concat)))
  ([state player-id]
   {:pre [(map? state)]}
   (get-in state [:players player-id :tiles])))

(defn add-tile
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
   (-> state
       (update-in [:players owner-id :tiles]
                  (fn [tiles]
                    (conj tiles {:name        tile-name
                                 :owner-id    owner-id
                                 :troop-count troop-count}))))))

(defn add-tiles
  {:test (fn []
           (is= (as-> (create-empty-state 3) $
                      (add-tiles $ 1 ["Indonesia" "New Guinea"] 1)
                      (get-in $ [:players 1 :tiles])
                      (map :name $))
                ["Indonesia" "New Guinea"]))}
  ([state owner-id tile-names]
   (add-tiles state owner-id tile-names 1))
  ([state owner-id tile-names troop-count]
   {:pre [(map? state) (every? string? tile-names) (pos-int? troop-count)]}
   (reduce (fn [state tile-name]
             (add-tile state owner-id tile-name troop-count))
           state
           tile-names)))

(defn get-tile
  {:test (fn []
           (is= (-> (create-empty-state 2)
                    (add-tile 1 "Indonesia" 1)
                    (get-tile "Indonesia"))
                {:name        "Indonesia"
                 :owner-id    1
                 :troop-count 1}))}
  [state tile-name]
  {:pre [(map? state) (string? tile-name)]}
  (->> (get-tiles state)
       (filter (fn [t] (= (:name t) tile-name)))
       (first)))

(defn get-owner-id
  {:test (fn []
           (is= (-> (create-empty-state 2)
                    (add-tile 1 "Indonesia")
                    (get-owner-id "Indonesia"))
                1))}
  [state tile-name]
  {:pre [(map? state) (string? tile-name)]}
  (:owner-id (get-tile state tile-name)))

(defn get-troop-count
  {:test (fn []
           (is= (-> (create-empty-state 2)
                    (add-tile 2 "Indonesia" 10)
                    (get-troop-count "Indonesia"))
                10))}
  [state tile-name]
  {:pre [(map? state) (string? tile-name)]}
  (:troop-count (get-tile state tile-name)))

(defn get-cards
  {:test (fn []
           (is= (-> (create-empty-state 3)
                    (get-cards 1))
                {:a 0
                 :b 0
                 :c 0}))}
  ([state player-id]
   {:pre [(map? state)]}
   (get-in state [:players player-id :cards])))

(defn add-card
  {:test (fn []
           (is= (-> (create-empty-state 3)
                    (add-card 1 :a)
                    (get-cards 1)
                    (:a))
                1))}
  [state player-id card-type]
  {:pre [(map? state) (keyword? card-type)]}
  (update-in state [:players player-id :cards card-type] inc))

(defn add-cards
  {:test (fn []
           (is= (-> (create-empty-state 3)
                    (add-cards 1 {:a 1 :b 2})
                    (add-cards 1 {:a 2 :b 0})
                    (get-cards 1))
                {:a 3 :b 2 :c 0}))}
  [state player-id cards]
  {:pre [(map? state) (map? cards)]}
  (let [update-fn (fn [state card-type quantity]
                    (if (contains? cards card-type)
                      (update-in state
                                 [:players player-id :cards card-type]
                                 (fn [x] (+ x quantity)))
                      state))]
    (reduce-kv update-fn state cards)))

(defn remove-card
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
         player-count (get-player-count state)
         [seed tile-name-partns] (random-partition-with-seed seed player-count tile-names)
         state (assoc state :seed seed)
         indexed-partns (map-indexed (fn [index part]
                                       {:player-id (inc index)
                                        :partition part})
                                     tile-name-partns)]
     (reduce (fn [state {id         :player-id
                         tile-names :partition}]
               (add-tiles state id tile-names))
             state
             indexed-partns))))

(defn create-game
  "Creates a starting games state by randomly assigning territories."
  {:test (fn []
           (is= (create-game 2)
                {:player-in-turn 1
                 :turn-phase     :card-exchange-phase
                 :seed           -9203025489357073502
                 :players        {1 {:id    1
                                     :tiles [{:name        "Indonesia"
                                              :owner-id    1
                                              :troop-count 1}
                                             {:name        "Western Australia"
                                              :owner-id    1
                                              :troop-count 1}]
                                     :cards {:a 0
                                             :b 0
                                             :c 0}}
                                  2 {:id    2
                                     :tiles [{:name        "New Guinea"
                                              :owner-id    2
                                              :troop-count 1}
                                             {:name        "Eastern Australia"
                                              :owner-id    2
                                              :troop-count 1}]
                                     :cards {:a 0
                                             :b 0
                                             :c 0}}}
                 :rules          {:initial-army-size          20
                                  :initial-reinforcement-size 5
                                  :initial-card-exchange-rate 4}}))}
  [num-players]
  {:pre [(>= num-players 2)]}
  (-> (create-empty-state num-players)
      (randomly-assign-tiles)))

(defn create-test-game
  "Always returns the same state, for testing."
  []
  {:player-in-turn 1
   :turn-phase     :card-exchange-phase
   :seed           -9203025489357073502
   :players        {1 {:id    1
                       :tiles [{:name        "Indonesia"
                                :owner-id    1
                                :troop-count 2}
                               {:name        "Western Australia"
                                :owner-id    1
                                :troop-count 1}]
                       :cards {:a 0
                               :b 0
                               :c 0}}
                    2 {:id    2
                       :tiles [{:name        "New Guinea"
                                :owner-id    2
                                :troop-count 1}
                               {:name        "Eastern Australia"
                                :owner-id    2
                                :troop-count 1}]
                       :cards {:a 0
                               :b 0
                               :c 0}}}
   :rules          {:initial-army-size          20
                    :initial-reinforcement-size 5
                    :initial-card-exchange-rate 4}})

(defn get-tiles-from-names
  {:test (fn []
           (is= (-> (create-game 2)
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
  (map (fn [tile-name]
         (get-tile state tile-name))
       names))

(defn get-region-bonus
  "Returns the region bonus of a specified region."
  {:test (fn []
           (is= (get-region-bonus "Australia")
                2))}
  [region-name]
  (:region-bonus (get-region-defn region-name)))

; TODO make test not definition/map dependent.
(defn owns-region?
  "Checks if a player owns a region."
    {:test (fn []
             (not (-> (create-game 2)
                      (owns-region? 1 "Australia")))
             (is (-> (create-empty-state 2)
                      (add-tiles 1 ["Indonesia" "Western Australia" "New Guinea" "Eastern Australia"])
                      (owns-region? 1 "Australia"))))}
  [state player-id region-name]
  (->> (get-region-defn region-name)
       (:tiles)
       (get-tiles-from-names state)
       (map (fn [tile] (:owner-id tile)))
       (filter (fn [owner-id] (not= owner-id player-id)))
       (count)
       (= 0)))

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
    (filter (fn [region-name] (owns-region? state player-id region-name)) region-names)))

(defn get-player-region-bonuses
  "Returns the total region bonuses the player has."
    {:test (fn []
             (is= (-> (create-game 2)
                      (get-player-region-bonuses 1))
                  0)
             (is= (-> (create-empty-state 2)
                      (add-tiles 1 ["Indonesia" "Western Australia" "New Guinea" "Eastern Australia"])
                      (get-player-region-bonuses 1))
                  2))}
  [state player-id]
  (->> (get-owned-regions state player-id)
       (map (fn [region-name] (get-region-bonus region-name)))
       (reduce (fn [region-bonus curr] (+ curr region-bonus)) 0)))