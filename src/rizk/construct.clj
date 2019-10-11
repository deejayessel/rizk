(ns rizk.construct
  (:require [ysera.test :refer [is= is is-not error?]]
            [rizk.definitions :refer [get-all-territory-defns
                                      get-territory-defn]]
            [rizk.random :refer [random-partition-with-seed]]))

(defn create-empty-state
  ; TODO player names?
  {:test (fn []
           (is= (create-empty-state 2)
                {:player-in-turn 1
                 :turn-phase     :distribution-phase
                 :seed           0
                 :players        {1 {:id          1
                                     :territories []
                                     :cards       {:a 0
                                                   :b 0
                                                   :c 0}}
                                  2 {:id          2
                                     :territories []
                                     :cards       {:a 0
                                                   :b 0
                                                   :c 0}}}
                 :ownership-map  {}
                 :rules          {:initial-army-size          20
                                  :initial-reinforcement-size 5
                                  :initial-card-exchange-rate 4}}))}
  [num-players]
  {:pre [(int? num-players)
         (>= num-players 2)]}
  {:player-in-turn 1
   :turn-phase     :distribution-phase
   :seed           0
   :players        (->> (range 1 (+ num-players 1))
                        (map-indexed (fn [index player-num]
                                       {:id          player-num
                                        :territories []
                                        :cards       {:a 0 :b 0 :c 0}}))
                        (reduce (fn [a v]
                                  (assoc a (:id v) v))
                                {}))
   :ownership-map  {}
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
  (let [state (create-empty-state 3)]
    (-> (:players state)
        (vals))))

(defn get-player-count
  {:test (fn []
           (is= (-> (create-empty-state 3)
                    (get-player-count))
                3))}
  [state]
  (let [players (get-players state)]
    (count players)))

(defn get-player
  {:test (fn []
           (is= (-> (create-empty-state 3)
                    (get-player 2))
                {:id          2
                 :territories []
                 :cards       {:a 0
                               :b 0
                               :c 0}}))}
  [state player-id]
  {:pre [(pos-int? player-id) (<= 1 player-id (get-player-count state))]}
  (get-in state [:players player-id]))

; TODO get-territories
; TODO get-neighbors

;(defn randomly-assign-territories
;  "Randomly assigns all territories to players in game.
;   All territories should be assigned, each territory
;   should have only one owner."
;  {:test (fn []
;           (let [state (-> (create-empty-state 2)
;                           (randomly-assign-territories))]
;             ; TODO All territories assigned
;             ; TODO Each territory has one owner
;             ; TODO Number of territories is balanced
;             ))}
;  [state]
;  state
;  )

(defn get-territories
  {:test (fn []
           (is= (-> (create-empty-state 3)
                    (get-territories 1))
                []))}
  [state player-id]
  (get-in state [:players player-id :territories]))

(defn add-territory
  {:test (fn []
           (let [state (-> (create-empty-state 3)
                           (add-territory "Indonesia" 1 1))]
             ; players updated
             (is= (-> state
                      (get-territories 1))
                  ["Indonesia"])
             ; ownership-map updated
             (is= (:ownership-map state)
                  {"Indonesia" {:name        "Indonesia"
                                :owner-id    1
                                :troop-count 1}}))
           (let [state (create-empty-state 3)]
             (is= (-> state
                      (add-territory "Indonesia" 1))
                  (-> state
                      (add-territory "Indonesia" 1)))))}
  ([state territory-name owner-id]
   (add-territory state territory-name owner-id 1))
  ([state territory-name owner-id troop-count]
   (-> state
       (update-in [:players owner-id :territories]          ; update players
                  (fn [territories]
                    (conj territories territory-name)))
       (update :ownership-map                               ; update map
               (fn [ownership-map]
                 (assoc ownership-map
                   territory-name
                   {:name        territory-name
                    :owner-id    owner-id
                    :troop-count troop-count}))))))

(defn add-territories
  {:test (fn []
           (let [state (-> (create-empty-state 3)
                           (add-territories ["Indonesia" "New Guinea"] 1 1))]
             ; players updated
             (is= (get-territories state 1)
                  ["Indonesia" "New Guinea"])
             ; ownership-map updated
             (is= (:ownership-map state)
                  {"Indonesia"  {:name        "Indonesia"
                                 :owner-id    1
                                 :troop-count 1}
                   "New Guinea" {:name        "New Guinea"
                                 :owner-id    1
                                 :troop-count 1}})))}

  ([state territory-names owner-id]
   (add-territories state territory-names owner-id 1))
  ([state territory-names owner-id troop-count]
   (reduce (fn [state territory-name]
             (add-territory state territory-name owner-id troop-count))
           state
           territory-names)))

(defn create-game
  {:test (fn []
           (is= (create-game 2)
                {:player-in-turn 1
                 :turn-phase     :distribution-phase
                 :seed           0
                 :players        {1 {:id          1
                                     :territories ["Indonesia"
                                                   "Western Australia"]
                                     :cards       {:a 0
                                                   :b 0
                                                   :c 0}}
                                  2 {:id          2
                                     :territories ["New Guinea"
                                                   "Eastern Australia"] ;TODO
                                     :cards       {:a 0
                                                   :b 0
                                                   :c 0}}}
                 :ownership-map  {"Indonesia"         {:name        "Indonesia"
                                                       :owner-id    1
                                                       :troop-count 1}
                                  "New Guinea"        {:name        "New Guinea"
                                                       :owner-id    2
                                                       :troop-count 1}
                                  "Western Australia" {:name        "Western Australia"
                                                       :owner-id    1
                                                       :troop-count 1}
                                  "Eastern Australia" {:name        "Eastern Australia"
                                                       :owner-id    2
                                                       :troop-count 1}
                                  }                         ;TODO
                 :rules          {:initial-army-size          20
                                  :initial-reinforcement-size 5
                                  :initial-card-exchange-rate 4}}))}
  [num-players]
  (let [state (create-empty-state num-players)
        seed (:seed state)
        territory-names (->> (get-all-territory-defns)
                             (map :name))
        [seed territory-name-partitions] (random-partition-with-seed seed num-players territory-names)
        indexed-partitions (map-indexed (fn [i x] (list (+ i 1) x))
                                        territory-name-partitions)]

    (-> (reduce (fn [state index-partition]
                  (let [[player-index territory-names] index-partition]
                    (add-territories state territory-names player-index)))
                state
                indexed-partitions))))
