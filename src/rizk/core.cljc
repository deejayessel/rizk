(ns rizk.core
  (:require [ysera.test :refer [is= is is-not error?]]
            [ysera.error :refer [error]]
            [rizk.util :refer [non-neg-int?]]
            [rizk.definitions :refer [get-all-node-defns
                                      get-node-defn
                                      get-region-defn]]
            [rizk.construct :refer [active-player-id
                                    add-nodes
                                    create-game
                                    create-node
                                    get-owned-regions
                                    neighbor-names
                                    get-node
                                    get-nodes
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
           (is (-> (create-game 2 [] :turn-phase :card-exchange-phase)
                   (can-draw-card? "p1"))))}
  [state player-id]
  {:pre [(map? state) (string? player-id)]}
  (and (= (active-player-id state)
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
           (is= (-> (create-game 2 [{:nodes ["i"
                                             "iii"]}
                                    {:nodes ["ii"
                                             "iii"]}])
                    (get-player-region-bonuses "p1"))
                0)
           (is= (-> (create-game 2 [{:nodes ["i"
                                             "ii"
                                             "iii"
                                             "iv"]}])
                    (get-player-region-bonuses "p1"))
                4))}
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
           (is= (-> (create-game 2 [{:nodes ["i" "ii"]}
                                    {:nodes ["iii" "iv"]}])
                    (reinforcement-count "p1"))
                3)
           (is= (-> (create-game 2 [{:nodes ["i" "ii" "iii" "iv"]}])
                    (reinforcement-count "p1"))
                7))}
  [state player-id]
  {:pre [(map? state) (string? player-id)]}
  (let [node-count (-> (get-nodes state player-id)
                       (count))
        region-bonus (get-player-region-bonuses state player-id)]
    (+ (max 3 (quot node-count 3))
       region-bonus)))

(defn valid-attack?
  "Checks if a move is a valid attack.

  1. Src node is owned by player,
  2. Dest node is owned by another player,
  3. Src node has 2 or more troops,
  4. Src and Dest are neighbors,
  5. Player in turn is in attack phase
  6. Attacker is in turn."
  {:test (fn []
           (let [state (create-game 2 [{:nodes ["i" "iii"]}
                                       {:nodes ["ii" "iii"]}]
                                    :turn-phase :attack-phase)]

             ; Must attack from friendly territory
             (is-not (valid-attack? state "p1" "ii" "iii"))
             ; Must attack unfriendly territory
             (is-not (valid-attack? state "p1" "i" "iii"))
             ; Must have more than one troop in territory
             (is-not (valid-attack? state "p1" "iii" "ii"))
             ; Territories must be neighbors
             (is-not (valid-attack? state "p1" "i" "iii")))

           ; Must be in attack phase
           (is-not (-> (create-game 2 [{:nodes [(create-node "i" :troop-count 2)
                                                "iii"]}
                                       {:nodes ["ii" "iii"]}]
                                    :turn-phase :movement-phase)
                       (valid-attack? "p1" "i" "ii")))

           ; Valid attack
           (is (-> (create-game 2 [{:nodes [(create-node "i"
                                                         :troop-count 10)
                                            "iii"]}
                                   {:nodes ["ii" "iii"]}]
                                :turn-phase :attack-phase)
                   (valid-attack? "p1" "i" "ii"))))}
  [state player-id src-name dst-name]
  {:pre [(map? state) (string? player-id) (string? src-name) (string? dst-name)]}
  (let [src-node (get-node state src-name)
        dst-node (get-node state dst-name)]
    (and (= (active-player-id state) player-id)
         (= (:turn-phase state) :attack-phase)
         (= (:owner-id src-node)
            player-id)
         (not= (:owner-id dst-node)
               player-id)
         (> (:troop-count src-node) 1)
         (neighbors? src-name dst-name))))
