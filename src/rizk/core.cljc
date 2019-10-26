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

(defn can-draw-card?
  "Determines whether a player can draw a card."
  {:test (fn []
           ; player must be in turn
           (is-not (-> (create-game 2)
                       (can-draw-card? "p2")))
           ; player must be in card-exchange phase
           (is-not (-> (create-game 2 [] :turn-phase :attack-phase)
                       (can-draw-card? "p1")))
           ; player can draw card
           (is (-> (create-game 2)
                   (can-draw-card? "p1"))))}
  [state player-id]
  {:pre [(map? state) (string? player-id)]}
  (and (= (get-player-id-in-turn state)
          player-id)
       (= (:turn-phase state)
          :card-exchange-phase)))

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

(defn get-player-region-bonuses
  "Returns the total region bonuses the player has."
  {:test (fn []
           (is= (-> (create-game 2 [{:tiles ["Indonesia"
                                             "Western Australia"]}
                                    {:tiles ["New Guinea"
                                             "Eastern Australia"]}])
                    (get-player-region-bonuses "p1"))
                0)
           (is= (-> (create-game 2 [{:tiles ["New Guinea"
                                             "Indonesia"
                                             "Western Australia"
                                             "Eastern Australia"]}])
                    (get-player-region-bonuses "p1"))
                2))}
  [state player-id]
  {:pre [(map? state) (string? player-id)]}
  (->> (get-owned-regions state player-id)
       (map (fn [region-name]
              (let [region-defn (get-region-defn region-name)]
                (:region-bonus region-defn))))
       (apply +)))

(defn reinforcement-count
  "Determines the number of reinforcements a given player receives on their turn.
    Each player receives a minimum of 3 troops.  Otherwise, their reinforcement count
    is determined by the number of territories they have divided by 3. In addition to this,
    they gain extra troops granted by any region bonuses."
  {:test (fn []
           (is= (-> (create-game 2 [{:tiles ["New Guinea"
                                             "Indonesia"]}
                                    {:tiles ["Western Australia"
                                             "Eastern Australia"]}])
                    (reinforcement-count "p1"))
                3)
           (is= (-> (create-game 2 [{:tiles ["New Guinea"
                                             "Indonesia"
                                             "Western Australia"
                                             "Eastern Australia"]}])
                    (reinforcement-count "p1"))
                5))}
  [state player-id]
  {:pre [(map? state) (string? player-id)]}
  (let [tile-count (-> (get-tiles state player-id)
                       (count))
        region-bonus (get-player-region-bonuses state player-id)]
    (+ (max 3 (quot tile-count 3))
       region-bonus)))

(defn valid-attack?
  "Checks if a move is a valid attack.

  1. Src tile is owned by player,
  2. Dest tile is owned by another player,
  3. Src tile has 2 or more troops,
  4. Src and Dest are neighbors,
  5. Player in turn is in attack phase
  6. Attacker is in turn."
  {:test (fn []
           (let [state (create-game 2 [{:tiles ["Indonesia" "Western Australia"]}
                                       {:tiles ["New Guinea" "Eastern Australia"]}]
                                    :turn-phase :attack-phase)]

             ; Must attack from friendly territory
             (is-not (valid-attack? state "p1" "New Guinea" "Eastern Australia"))
             ; Must attack unfriendly territory
             (is-not (valid-attack? state "p1" "Indonesia" "Western Australia"))
             ; Must have more than one troop in territory
             (is-not (valid-attack? state "p1" "Western Australia" "New Guinea"))
             ; Territories must be neighbors
             (is-not (valid-attack? state "p1" "Indonesia" "Eastern Australia")))

           ; Must be in attack phase
           (is-not (-> (create-game 2 [{:tiles [(create-tile "Indonesia" :troop-count 2)
                                                "Western Australia"]}
                                       {:tiles ["New Guinea" "Eastern Australia"]}]
                                    :turn-phase :card-exchange-phase)
                       (valid-attack? "p1" "Indonesia" "New Guinea")))

           ; Valid attack
           (is (-> (create-game 2 [{:tiles [(create-tile "Indonesia"
                                                         :troop-count 10)
                                            "Western Australia"]}
                                   {:tiles ["New Guinea" "Eastern Australia"]}]
                                :turn-phase :attack-phase)
                   (valid-attack? "p1" "Indonesia" "New Guinea"))))}
  [state player-id src-name dst-name]
  {:pre [(map? state) (string? player-id) (string? src-name) (string? dst-name)]}
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
