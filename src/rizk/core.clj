(ns rizk.core
  (:require [ysera.test :refer [is= is is-not error?]]
            [ysera.error :refer [error]]
            [rizk.util :refer [non-neg-int?]]
            [rizk.definitions :refer [get-all-tile-defns
                                      get-tile-defn]]
            [rizk.construct :refer [create-game
                                    create-test-game
                                    get-player-id-in-turn
                                    get-tiles
                                    get-tile
                                    get-troop-count
                                    get-owner-id
                                    neighbors?]]))

(defn valid-trade?
  "Checks if a set of cards forms a valid trade.  Players trade in hands of 3 cards:
   a hand must have either one card of each type (i.e., `A-B-C`) or three cards of
   the same type (e.g. `A-A-A`)"
  {:test (fn []
           (is (valid-trade? {:a 1 :b 1 :c 1}))
           (is (valid-trade? {:a 3 :b 0 :c 0}))
           (is-not (valid-trade? {:a 2 :b 1 :c 1})))}
  [{a :a b :b c :c}]
  {:pre [() (not (neg-int? b)) (not (neg-int? c))]}
  (if (= (+ a b c) 3)
    (or (= a 3)
        (= b 3)
        (= c 3)
        (and (= a 1) (= b 1) (= c 1)))
    nil))

; TODO : get-region-bonuses
(comment
  (defn reinforcement-count
    "Determines the number of reinforcements a given player receives on their turn.
    Each player receives a minimum of 3 troops.  Otherwise, their reinforcement count
    is determined by the number of territories they have divided by 3, in addition
    to any region bonuses."
    {:test (fn []
             (is= (-> (create-test-game)
                      (reinforcement-count 1))
                  3))}
    [state player-id]
    (let [tile-count (-> (get-tiles state player-id)
                         (count))
          region-bonus (get-region-bonuses state player-id)]
      (max 3                                                ; minimum allotment of 3 troops
           (+ (quot tile-count 3)
              region-bonus)))))

(defn valid-attack?
  "Checks if a move is a valid attack. This involves 1. Initial location is owned by player, 2.
  Final territory is owned by another player, 3. Initial location has 2 or more troops, 4.
  Initial location and final location are neighbors."
  {:test (fn []
           ; Must attack from friendly territory
           (is-not (-> (create-test-game)
                       (valid-attack? 1 "New Guinea" "Eastern Australia")))
           ; Must attack unfriendly territory
           (is-not (-> (create-test-game)
                       (valid-attack? 1 "Indonesia" "Western Australia")))
           ; Must have more than one troop in territory
           (is-not (-> (create-test-game)
                       (valid-attack? 1 "Western Australia" "New Guinea")))
           ; Territories must be neighbors
           (is-not (-> (create-test-game)
                       (valid-attack? 1 "Indonesia" "Eastern Australia")))
           ; Valid attack
           (is (-> (create-test-game)
                   (valid-attack? 1 "Indonesia" "New Guinea"))))}
  [state player-id src-name dst-name]
  {:pre [(map? state) (string? src-name) (string? dst-name)
         (= (get-player-id-in-turn state) player-id)]}
  (let [src-owner-id (get-owner-id state src-name)
        dst-owner-id (get-owner-id state dst-name)]
    (and (= src-owner-id
            player-id)
         (not= dst-owner-id
               player-id)
         (> (get-troop-count state src-name) 1)
         (neighbors? src-name dst-name))))

; TODO
(defn attack
  {:test (fn [])}
  [state attacker-id src-name dst-name]
  ;(if (valid-attack? state attacker-id src-name dst-name)
  ;  ; TODO attack
  ;  state)
  (error "Not implemented"))
