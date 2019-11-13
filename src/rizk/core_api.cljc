(ns rizk.core-api
  (:require [ysera.test :refer [is= is is-not error?]]
            [ysera.error :refer [error]]
            [rizk.util :refer [dec-by]]
            [rizk.random-state :refer [roll-n-dice]]
            [rizk.construct :refer [create-game
                                    create-tile
                                    get-owned-groups
                                    active-player-id
                                    neighbor-names
                                    get-tile
                                    get-tiles
                                    neighbors?
                                    update-tile
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
  "Attacks once from src-tile to dst-tile.

  Uses Lanchester's linear law to determine chances of success."
  {:test (fn []
           (let [state (-> (create-game 2 [{:tiles [(create-tile "i" :troop-count 5)]}
                                           {:tiles [(create-tile "ii" :troop-count 3)]}])
                           (go-to-next-phase)
                           (attack-once "p1" "i" "ii"))]
             (is= (-> (get-tile state "i")
                      (:troop-count))
                  4)
             (is= (-> (get-tile state "ii")
                      (:troop-count))
                  2)))}
  [state attacker-id src-name dst-name]
  {:pre [(map? state) (every? string? [attacker-id src-name dst-name])]}
  (if-not (valid-attack? state attacker-id src-name dst-name)
    (error "Invalid attack.")
    (let []
      )))

(defn attack-k-times
  "Attacks k times from the src-tile tothe dst-tile."
  {:test (fn []
           (let [state (-> (create-game 2 [{:tiles [(create-tile "i" :troop-count 5)]}
                                           {:tiles [(create-tile "ii" :troop-count 3)]}])
                           (go-to-next-phase)
                           (attack-once "p1" "i" "ii"))]
             (is= (-> (get-tile state "i")
                      (:troop-count))
                  4)
             (is= (-> (get-tile state "ii")
                      (:troop-count))
                  2)))}
  )