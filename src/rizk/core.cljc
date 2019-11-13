(ns rizk.core
  (:require [ysera.test :refer [is= is is-not error?]]
            [ysera.error :refer [error]]
            [rizk.util :refer [non-neg-int?]]
            [rizk.definitions :refer [get-all-tile-defns
                                      get-tile-defn
                                      get-group-defn]]
            [rizk.construct :refer [active-player-id
                                    add-tiles
                                    create-game
                                    create-tile
                                    get-owned-groups
                                    get-tile
                                    get-tiles
                                    neighbor-names
                                    neighbors?]]))

(defn get-player-group-bonuses
  "Returns the total group bonuses the player has."
  {:test (fn []
           (is= (-> (create-game 2 [{:tiles ["i" "iii"]}
                                    {:tiles ["ii" "iii"]}])
                    (get-player-group-bonuses "p1"))
                0)
           (is= (-> (create-game 2 [{:tiles ["i" "ii" "iii" "iv"]}])
                    (get-player-group-bonuses "p1"))
                4))}
  [state player-id]
  {:pre [(map? state) (string? player-id)]}
  (->> (get-owned-groups state player-id)
       (map (fn [group-name]
              (let [group-defn (get-group-defn group-name)]
                (:group-bonus group-defn))))
       (apply +)))

(defn reinforcement-count
  "Determines the number of reinforcements a given player receives on their turn.
    Each player receives a minimum of 3 troops.  Otherwise, their reinforcement count
    is determined by the number of tiles they have divided by 3. In addition to this,
    they gain extra troops granted by any group bonuses."
  {:test (fn []
           (is= (-> (create-game 2 [{:tiles ["i" "ii"]}
                                    {:tiles ["iii" "iv"]}])
                    (reinforcement-count "p1"))
                3)
           (is= (-> (create-game 2 [{:tiles ["i" "ii" "iii" "iv"]}])
                    (reinforcement-count "p1"))
                7))}
  [state player-id]
  {:pre [(map? state) (string? player-id)]}
  (let [tile-count (-> (get-tiles state player-id)
                       (count))
        group-bonus (get-player-group-bonuses state player-id)]
    (+ (max 3 (quot tile-count 3))
       group-bonus)))

(defn valid-attack?
  "Checks if a move is a valid attack.

  1. Src tile is owned by player,
  2. Dest tile is owned by another player,
  3. Src tile has 2 or more troops,
  4. Src and Dest are neighbors,
  5. Player in turn is in attack phase
  6. Attacker is in turn."
  {:test (fn []
           (let [state (create-game 2 [{:tiles ["i" "iii"]}
                                       {:tiles ["ii" "iii"]}]
                                    :turn-phase :attack-phase)]

             ; Must attack from friendly tile
             (is-not (valid-attack? state "p1" "ii" "iii"))
             ; Must attack unfriendly tile
             (is-not (valid-attack? state "p1" "i" "iii"))
             ; Must have more than one troop in tile
             (is-not (valid-attack? state "p1" "iii" "ii"))
             ; Territories must be neighbors
             (is-not (valid-attack? state "p1" "i" "iii")))

           ; Must be in attack phase
           (is-not (-> (create-game 2 [{:tiles [(create-tile "i" :troop-count 2)
                                                "iii"]}
                                       {:tiles ["ii" "iii"]}]
                                    :turn-phase :movement-phase)
                       (valid-attack? "p1" "i" "ii")))

           ; Valid attack
           (is (-> (create-game 2 [{:tiles [(create-tile "i" :troop-count 10)
                                            "iii"]}
                                   {:tiles ["ii" "iii"]}]
                                :turn-phase :attack-phase)
                   (valid-attack? "p1" "i" "ii"))))}
  [state player-id src-name dst-name]
  {:pre [(map? state) (string? player-id) (string? src-name) (string? dst-name)]}
  (let [src-tile (get-tile state src-name)
        dst-tile (get-tile state dst-name)]
    (and (= (active-player-id state) player-id)
         (= (:turn-phase state) :attack-phase)
         (= (:owner-id src-tile)
            player-id)
         (not= (:owner-id dst-tile)
               player-id)
         (> (:troop-count src-tile) 1)
         (neighbors? src-name dst-name))))
