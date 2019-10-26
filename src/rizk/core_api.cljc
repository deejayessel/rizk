(ns rizk.core-api
  (:require [ysera.test :refer [is= is is-not error?]]
            [ysera.error :refer [error]]
            [rizk.util :refer [dec-by]]
            [rizk.random-state :refer [get-random-card
                                       roll-n-dice]]
            [rizk.construct :refer [add-card
                                    create-game
                                    create-tile
                                    get-cards
                                    get-owned-regions
                                    get-player-id-in-turn
                                    get-neighbor-names
                                    get-tile
                                    get-tiles
                                    neighbors?
                                    update-tile
                                    update-turn-phase]]
            [rizk.core :refer [can-draw-card?
                               valid-attack?]]))

(defn draw-card
  "Draw a card for the player."
  {:test (fn []
           (error? (-> (create-game 2)
                       (draw-card "p2")))
           (is= (-> (create-game 2)
                    (draw-card "p1")
                    (get-cards "p1")
                    (:b))
                1))}
  [state player-id]
  {:pre [(map? state) (string? player-id)]}
  (if-not (can-draw-card? state player-id)
    (error "Cannot draw card.")
    (let [[state card-type] (get-random-card state)]
      (add-card state player-id card-type))))

(defn advance-to-next-phase
  "Moves on to the next turn phase."
  {:test (fn []
           (is= (-> (create-game 2)
                    (advance-to-next-phase)
                    (:turn-phase))
                :attack-phase)
           (is= (-> (create-game 2)
                    (advance-to-next-phase)
                    (advance-to-next-phase)
                    (:turn-phase))
                :coordination-phase))}
  [state]
  {:pre [(map? state)]}
  (update-turn-phase state
                     (fn [phase]
                       (if (= phase :coordination-phase)
                         (error "Tried to advance past coordination phase")
                         (phase {:card-exchange-phase :attack-phase
                                 :attack-phase        :coordination-phase})))))

(defn attack-once
  "Attacks once from src-tile to dst-tile."
  {:test (fn []
           (let [state (-> (create-game 2 [{:tiles [(create-tile "Indonesia" :troop-count 5)]}
                                           {:tiles [(create-tile "New Guinea" :troop-count 3)]}])
                           (advance-to-next-phase)
                           (attack-once "p1" "Indonesia" "New Guinea"))]
             (is= (-> (get-tile state "Indonesia")
                      (:troop-count))
                  4)
             (is= (-> (get-tile state "New Guinea")
                      (:troop-count))
                  2)))}
  [state attacker-id src-name dst-name]
  {:pre [(map? state) (every? string? [attacker-id src-name dst-name])]}
  (if-not (valid-attack? state attacker-id src-name dst-name)
    (error "Invalid attack.")
    (let [src-tile (get-tile state src-name)
          dst-tile (get-tile state dst-name)

          ; determine attacker/defender dice counts
          attacker-dice-count (min 3 (:troop-count src-tile))
          defender-dice-count (min 2 (:troop-count dst-tile))

          ; roll dice
          [state attacker-rolls] (roll-n-dice state attacker-dice-count)
          [state defender-rolls] (roll-n-dice state defender-dice-count)

          ; sort decreasing
          attacker-rolls (take 2 (sort (comp - compare) attacker-rolls))
          defender-rolls (sort (comp - compare) defender-rolls)

          attacker-win-count (->> (map vector attacker-rolls defender-rolls) ; zip
                                  (filter (fn [[attacker-roll defender-roll]]
                                            (> attacker-roll defender-roll))) ;; ties go to defender
                                  (count))
          defender-win-count (- 2 attacker-win-count)]
      (-> state
          (update-tile dst-name :troop-count (fn [x] (- x attacker-win-count)))
          (update-tile src-name :troop-count (fn [x] (- x defender-win-count)))))))