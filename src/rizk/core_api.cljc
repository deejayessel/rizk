(ns rizk.core-api
  (:require [ysera.test :refer [is= is is-not error?]]
            [ysera.error :refer [error]]
            [rizk.util :refer [dec-by]]
            [rizk.random-state :refer [roll-n-dice]]
            [rizk.construct :refer [create-game
                                    create-node
                                    get-owned-regions
                                    active-player-id
                                    neighbor-names
                                    get-node
                                    get-nodes
                                    neighbors?
                                    update-node
                                    update-turn-phase]]
            [rizk.core :refer [valid-attack?]]))

(defn go-to-next-phase
  "Moves on to the next turn phase."
  {:test (fn []
           (is= (-> (create-game 2)
                    (go-to-next-phase)
                    (:turn-phase))
                :attack-phase)
           (is= (-> (create-game 2)
                    (go-to-next-phase)
                    (go-to-next-phase)
                    (:turn-phase))
                :movement-phase)
           (error? (-> (create-game 2)
                       (go-to-next-phase)
                       (go-to-next-phase)
                       (go-to-next-phase))))}
  [state]
  {:pre [(map? state)]}
  (update-turn-phase state
                     (fn [phase]
                       (if (= phase :movement-phase)
                         (error "Tried to advance past movement phase")
                         (phase {:reinforcement-phase :attack-phase
                                 :attack-phase        :movement-phase})))))

(defn attack-once
  "Attacks once from src-node to dst-node."
  {:test (fn []
           (let [state (-> (create-game 2 [{:nodes [(create-node "i" :troop-count 5)]}
                                           {:nodes [(create-node "ii" :troop-count 3)]}])
                           (go-to-next-phase)
                           (attack-once "p1" "i" "ii"))]
             (is= (-> (get-node state "i")
                      (:troop-count))
                  4)
             (is= (-> (get-node state "ii")
                      (:troop-count))
                  2)))}
  [state attacker-id src-name dst-name]
  {:pre [(map? state) (every? string? [attacker-id src-name dst-name])]}
  (if-not (valid-attack? state attacker-id src-name dst-name)
    (error "Invalid attack.")
    (let [src-node (get-node state src-name)
          dst-node (get-node state dst-name)

          ; determine attacker/defender dice counts
          a-dice (min 3 (:troop-count src-node))
          d-dice (min 2 (:troop-count dst-node))

          ; roll dice
          [state as] (roll-n-dice state a-dice)
          [state ds] (roll-n-dice state d-dice)

          ; sort decreasing
          as (take 2 (sort (comp - compare) as))
          ds (sort (comp - compare) ds)

          a-wins (->> (map vector as ds) ; zip
                                  (filter (fn [[a d]]
                                            (> a d))) ;; ties go to defender
                                  (count))
          d-wins (- 2 a-wins)]
      (-> state
          (update-node dst-name :troop-count (fn [x] (- x a-wins)))
          (update-node src-name :troop-count (fn [x] (- x d-wins)))))))

(defn attack-k-times
  "Attacks k times from the src-node tothe dst-node."
  {:test (fn []
           (let [state (-> (create-game 2 [{:nodes [(create-node "i" :troop-count 5)]}
                                           {:nodes [(create-node "ii" :troop-count 3)]}])
                           (go-to-next-phase)
                           (attack-once "p1" "i" "ii"))]
             (is= (-> (get-node state "i")
                      (:troop-count))
                  4)
             (is= (-> (get-node state "ii")
                      (:troop-count))
                  2)))}
  )