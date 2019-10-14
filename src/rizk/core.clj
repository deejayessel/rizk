(ns rizk.core
  (:require [ysera.test :refer [is= is is-not error?]]
            [rizk.definitions :refer [get-all-tile-defns
                                      get-tile
                                      get-tile-defn]]
            [rizk.construct :refer [get-tiles]]))

(defn valid-trade?
  "Checks if a set of cards forms a valid trade.  Players trade in hands of 3 cards:
   a hand must have either one card of each type (i.e., `A-B-C`) or three cards of 
   the same type (e.g. `A-A-A`)"
  {:test (fn []
           (is (valid-trade? {:a 1 :b 1 :c 1}))
           (is (valid-trade? {:a 3 :b 0 :c 0}))
           (is-not (valid-trade? {:a 2 :b 1 :c 1})))}
  [{a :a b :b c :c}]
  {:pre [(not (neg-int? a)) (not (neg-int? b)) (not (neg-int? c))]}
  (if (= (+ a b c) 3)
    (or (= a 3)
        (= b 3)
        (= c 3)
        (and (= a 1) (= b 1) (= c 1)))
    nil))

(defn reinforce
  ; TODO should there be a check such that this function can only be called once?
  ; TODO region bonuses
  "Reinforces a player's troop number."
  {:test (fn [])}
  [state])

(defn valid-attack?
  "Checks if a move is a valid attack. This involves 1. Initial location is owned by player, 2. 
  Final territory is owned by another player, 3. Initial location has 2 or more troops, 4. 
  Initial location and final location are neighbors."
  {:test (fn [])}
  [state initial-loc final-loc]
  (let [player-id (:player-in-turn state)
        initial-tile (get-tile state initial-loc)]
    (and (= (:owner-id initial-tile)
            player-id)
         (not= (:owner-id initial-tile)
               (player-id))
         (> (:troop-count initial-tile) 2)
         (contains? (:neighbors initial-tile) final-loc))))

(defn attack
  {:test (fn [])}
  [state initial-loc final-loc]
  (if (valid-attack? state initial-loc final-loc)
    accessor
    (error))
