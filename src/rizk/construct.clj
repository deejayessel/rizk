(ns rizk.construct
  (:require [ysera.test :refer [is= is is-not error?]]
            [rizk.definitions :refer [get-all-tile-defns
                                      get-tile-defn]]
            [rizk.random :refer [random-partition-with-seed]]))

(defn create-empty-state
  ; TODO player names?
  {:test (fn []
           (is= (create-empty-state 2)
                {:player-in-turn 1
                 :turn-phase     :distribution-phase
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
   :turn-phase     :distribution-phase
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

(defn get-neighbors
  {:test (fn []
           (is= (get-neighbors "Indonesia")
                ["New Guinea"
                 "Western Australia"]))}
  [tile-name]
  {:pre [(string? tile-name)]}
  (let [tile-defn (get-tile-defn tile-name)]
    (:neighbors tile-defn)))

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
                      (add-tile $ "Indonesia" 1 1)
                      (get-tiles $ 1)
                      (map :name $))
                ["Indonesia"]))}
  ([state tile-name owner-id]
   (add-tile state tile-name owner-id 1))
  ([state tile-name owner-id troop-count]
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
                      (add-tiles $ ["Indonesia" "New Guinea"] 1 1)
                      (get-in $ [:players 1 :tiles])
                      (map :name $))
                ["Indonesia" "New Guinea"]))}
  ([state tile-names owner-id]
   (add-tiles state tile-names owner-id 1))
  ([state tile-names owner-id troop-count]
   {:pre [(map? state) (every? string? tile-names) (pos-int? troop-count)]}
   (reduce (fn [state tile-name]
             (add-tile state tile-name owner-id troop-count))
           state
           tile-names)))

(defn get-tile
  {:test (fn []
           (is= (-> (create-empty-state 2)
                    (add-tile "Indonesia" 1 1)
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
                    (add-tile "Indonesia" 1)
                    (get-owner-id "Indonesia"))
                1))}
  [state tile-name]
  {:pre [(map? state) (string? tile-name)]}
  (:owner-id (get-tile state tile-name)))

(defn get-troop-count
  {:test (fn []
           (is= (-> (create-empty-state 2)
                    (add-tile "Indonesia" 2 10)
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
  [state player-id]
  {:pre [(map? state)]}
  (get-in state [:players player-id :cards]))

(defn add-card-to-player
  {:test (fn []
           (is= (-> (create-empty-state 3)
                    (add-card-to-player 1 :a)
                    (get-cards 1)
                    (:a))
                1))}
  [state player-id card-type]
  {:pre [(map? state) (keyword? card-type)]}
  (update-in state [:players player-id :cards card-type] inc))

(defn remove-card-from-player
  {:test (fn []
           (is= (-> (create-empty-state 3)
                    (add-card-to-player 1 :a)
                    (remove-card-from-player 1 :a)
                    (get-cards 1)
                    (vals))
                [0 0 0])
           (is= (-> (create-empty-state 3)
                    (add-card-to-player 1 :a)
                    (add-card-to-player 1 :a)
                    (remove-card-from-player 1 :a)
                    (get-cards 1)
                    (vals))
                [1 0 0]))}
  [state player-id card-type]
  {:pre [(map? state) (keyword? card-type)]}
  (update-in state [:players player-id :cards card-type] dec))

(defn create-game
  {:test (fn []
           (is= (create-game 2)
                {:player-in-turn 1
                 :turn-phase     :distribution-phase
                 :seed           0
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
  {:pre [(int? num-players) (>= num-players 2)]}
  (let [state (create-empty-state num-players)
        seed (:seed state)
        tile-names (map :name (get-all-tile-defns))
        [_ tile-name-partns] (random-partition-with-seed seed num-players tile-names)
        named-partitions (map-indexed (fn [index part]
                                        {:player-id (inc index)
                                         :partition part})
                                      tile-name-partns)]
    (reduce (fn [state {id   :player-id
                        tile-names :partition}]
              (add-tiles state tile-names id))
            state
            named-partitions)))

(comment
  ([data & kvs]
   {:pre [(or (nil? data) (vector? data))]}
   (let [players-data (map-indexed (fn [index player-data]
                                     (assoc player-data :player-id (str "p" (inc index))))
                                   data)
         state (as-> (create-empty-state) $
                     (reduce (fn [state {player-id :player-id
                                         mana      :mana
                                         minions   :minions
                                         deck      :deck
                                         hand      :hand}]
                               (-> (if mana
                                     (-> state
                                         (update-mana player-id mana)
                                         (update-max-mana player-id mana))
                                     state)
                                   (add-minions-to-board player-id minions)
                                   (add-cards-to-deck player-id deck)
                                   (add-cards-to-hand player-id hand)))
                             $
                             players-data))]
     (if (empty? kvs)
       state
       (apply assoc state kvs))))
  )
