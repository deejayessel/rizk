(ns rizk.core
  (:require [ysera.test :refer [is= is is-not error?]]
            [ysera.error :refer [error]]
            [rizk.util :refer [non-neg-int?]]
            [rizk.definitions :refer [get-all-tile-defns
                                      get-tile-defn
                                      get-region-defn]]
            [rizk.construct :refer [add-tiles
                                    create-game
                                    create-tile
                                    get-owned-regions
                                    get-player-id-in-turn
                                    get-neighbor-names
                                    get-tile
                                    get-tiles
                                    neighbors?]]))

(defn get-player-region-bonuses
  "Returns the total region bonuses the player has."
  {:test (fn []
           (is= (-> (create-game 2 [{:tiles ["Indonesia"
                                             "Western Australia"]}
                                    {:tiles ["New Guinea"
                                             "Eastern Australia"]}])
                    (get-player-region-bonuses 1))
                0)
           (is= (-> (create-game 2 [{:tiles ["Indonesia"
                                             "Western Australia"
                                             "New Guinea"
                                             "Eastern Australia"]}])
                    (get-player-region-bonuses 1))
                2))}
  [state player-id]
  (->> (get-owned-regions state player-id)
       (map (fn [region-name]
              (let [region-defn (get-region-defn region-name)]
                (:region-bonus region-defn))))
       (apply +)))

(defn valid-hand?
  "Checks if a set of cards forms a valid trade.  Players trade in hands of 3 cards:
   a hand must have either one card of each type (i.e., `A-B-C`) or three cards of
   the same type (e.g. `A-A-A`)"
  {:test (fn []
           (is (valid-hand? {:a 1 :b 1 :c 1}))
           (is (valid-hand? {:a 3 :b 0 :c 0}))
           (is-not (valid-hand? {:a 2 :b 1 :c 1})))}
  [{a :a b :b c :c}]
  {:pre [(non-neg-int? a) (non-neg-int? b) (non-neg-int? c)]}
  (and (= (+ a b c) 3)
       (or (= a 3)
           (= b 3)
           (= c 3)
           (and (= a 1) (= b 1) (= c 1)))))

(defn reinforcement-count
  "Determines the number of reinforcements a given player receives on their turn.
    Each player receives a minimum of 3 troops.  Otherwise, their reinforcement count
    is determined by the number of territories they have divided by 3, in addition
    to any region bonuses."
  {:test (fn []
           (is= (-> (create-game 2 [{:tiles ["New Guinea"
                                             "Indonesia"]}
                                    {:tiles ["Western Australia"
                                             "Eastern Australia"]}])
                    (reinforcement-count 1))
                3)
           (is= (-> (create-game 2 [{:tiles ["New Guinea"
                                             "Indonesia"
                                             "Western Australia"
                                             "Eastern Australia"]}])
                    (reinforcement-count 1))
                3))}
  [state player-id]
  (let [tile-count (-> (get-tiles state player-id)
                       (count))
        region-bonus (get-player-region-bonuses state player-id)]
    (max 3                                                  ; minimum allotment of 3 troops
         (+ (quot tile-count 3)
            region-bonus))))

(defn valid-attack?
  "Checks if a move is a valid attack. This involves 1. Initial location is owned by player, 2.
  Final territory is owned by another player, 3. Initial location has 2 or more troops, 4.
  Initial location and final location are neighbors."
  {:test (fn []
           (let [state (create-game 2 [{:tiles ["Indonesia" "Western Australia"]}
                                       {:tiles ["New Guinea" "Eastern Australia"]}])]

             ; Must attack from friendly territory
             (is-not (valid-attack? state 1 "New Guinea" "Eastern Australia"))
             ; Must attack unfriendly territory
             (is-not (valid-attack? state 1 "Indonesia" "Western Australia"))
             ; Must have more than one troop in territory
             (is-not (valid-attack? state 1 "Western Australia" "New Guinea"))
             ; Territories must be neighbors
             (is-not (valid-attack? state 1 "Indonesia" "Eastern Australia")))

           ; Valid attack
           (is (-> (create-game 2 [{:tiles [(create-tile "Indonesia"
                                                         :troop-count 10)
                                            "Western Australia"]}
                                   {:tiles ["New Guinea" "Eastern Australia"]}]
                                :turn-phase :attack-phase)
                   (valid-attack? 1 "Indonesia" "New Guinea"))))}
  [state player-id src-name dst-name]
  {:pre [(map? state) (string? src-name) (string? dst-name)]}
  (let [src-tile (get-tile state src-name)
        dst-tile (get-tile state dst-name)]
    (and (= (get-player-id-in-turn state) player-id)
         (= (:turn-phase state) :attack-phase)
         (= (:owner-id src-tile)
            player-id)
         (not= (:owner-id dst-tile)
               player-id)
         (> (:troop-count src-tile) 1)
         (neighbors? src-name dst-name))))

; TODO
;(defn attack
;  {:test (fn [])}
;  [state attacker-id src-name dst-name]
;  ;(if (valid-attack? state attacker-id src-name dst-name)
;  ;  ; TODO attack
;  ;  state)
;  (error "Not implemented"))
