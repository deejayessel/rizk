(ns rizk.construct
  (:require [rizk.definitions :refer [get-definition]]
            [ysera.test :refer [is= is is-not error?]]))

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
                 :map            {}
                 :rules          {:initial-army-size          20
                                  :initial-reinforcement-size 5
                                  :initial-card-exchange-rate 4}}))}
  ([num-players]
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
    :map            {}
    :rules          {:initial-army-size          20
                     :initial-reinforcement-size 5
                     :initial-card-exchange-rate 4}})
  ([num-players & kvs]
   (let [empty-state (create-empty-state num-players)]
     (apply assoc empty-state kvs))))

(defn get-territories
  []
  )

(defn get-player-count
  {:test (fn []
           (is= (-> (create-empty-state 3)
                    (get-player-count))
                3))}
  [state]
  (let [players (get state :players)]
    (count players)))

; TODO
(defn get-territory-count
  {:test (fn []
           (is= (-> (create-empty-state 2)
                    (get-territory-count))
                4))}
  [state]
  )

(defn randomly-assign-territories
  "Randomly assigns all territories to players in game.
   All territories should be assigned, each territory
   should have only one owner."
  {:test (fn []
           (let [state (-> (create-empty-state 2)
                           (randomly-assign-territories))]
             ; TODO All territories assigned
             ; TODO Each territory has one owner
             ; TODO Number of territories is balanced
             ))}
  [state]
  state
  )



(defn create-game
  {:test (fn []
           (is= (create-game 2)
                {:player-in-turn 1
                 :turn-phase     :distribution-phase
                 :seed           0
                 :players        {1 {:id          1
                                     :territories []        ;TODO
                                     :cards       {:a 0
                                                   :b 0
                                                   :c 0}}
                                  2 {:id          2
                                     :territories []        ;TODO
                                     :cards       {:a 0
                                                   :b 0
                                                   :c 0}}}
                 :map            {}                         ;TODO
                 :rules          {:initial-army-size          20
                                  :initial-reinforcement-size 5
                                  :initial-card-exchange-rate 4}}))}
  ; TODO
  ; make empty state
  ; assign territories to players (randomly or according to some rule based
  ; on game format)
  [num-players])