(ns rizk.core
  (:require [ysera.test :refer [is= is is-not error?]]
            [ysera.error :refer [error]]
            [rizk.util :refer [non-neg-int?]]
            [rizk.random-state :refer [random-int]]
            [rizk.definitions :refer [get-all-tile-defns
                                      get-in-defn
                                      get-tile-defn
                                      get-group-defn]]
            [rizk.construct :refer [add-tiles
                                    add-units
                                    count-units
                                    create-game
                                    create-tile
                                    create-unit
                                    get-in-tile
                                    get-owned-groups
                                    get-player-id-in-turn
                                    get-tile
                                    get-tiles
                                    neighbor-names
                                    neighbors?
                                    remove-units
                                    update-tile]]))

(comment replenish-unit-moves)

(defn player-reinforcement-bonus
  "Returns a player's total reinforcement bonuses."
  {:test (fn []
           (is= (-> (create-game 2 [{:tiles ["i" "iii"]}
                                    {:tiles ["ii" "iii"]}])
                    (player-reinforcement-bonus "p1"))
                0)
           (is= (-> (create-game 2 [{:tiles ["i" "ii" "iii" "iv"]}])
                    (player-reinforcement-bonus "p1"))
                4))}
  [state player-id]
  {:pre [(map? state) (string? player-id)]}
  (->> (get-owned-groups state player-id)
       (map (fn [g] (get-in-defn g :group-bonus)))
       (apply +)))

(defn get-reinforcement-count
  "Determines the number of reinforcements a given player receives on their turn.

   Each player receives a minimum of 3 units.  Otherwise, their reinforcement count
   is determined by the number of tiles they have divided by 3. In addition to this,
   they gain extra units granted by any group bonuses."
  {:test (fn []
           (is= (-> (create-game 2 [{:tiles ["i" "ii"]}
                                    {:tiles ["iii" "iv"]}])
                    (get-reinforcement-count "p1"))
                3)
           (is= (-> (create-game 2 [{:tiles ["i" "ii" "iii" "iv"]}])
                    (get-reinforcement-count "p1"))
                7))}
  [state player-id]
  {:pre [(map? state) (string? player-id)]}
  (let [tile-count (-> (get-tiles state player-id)
                       (count))
        group-bonus (player-reinforcement-bonus state player-id)]
    (+ (max 3 (quot tile-count 3))
       group-bonus)))

(defn valid-attack?
  "Checks if a move is a valid attack.

  1. Owner of src is in turn.
  2. Owner of src does not own dst.
  3. Src tile has 2 or more units.
  4. Dst tile has 1 or more units.
  5. Src and Dest are neighbors."
  {:test (fn []
           (let [state (create-game 2 [{:tiles ["i" "iii"]}
                                       {:tiles ["ii" "iii"]}]
                                    :turn-phase :attack-phase)]

             ; Must attack from friendly tile
             (is-not (valid-attack? state "ii" "iii"))
             ; Must attack unfriendly tile
             (is-not (valid-attack? state "i" "iii"))
             ; Must have more than one unit in src
             (is-not (valid-attack? state "iii" "ii"))
             ; Must have some units in dst
             (is-not (-> (update-tile state "ii" :units [])
                         (valid-attack? "iii" "ii")))
             ; Territories must be neighbors
             (is-not (valid-attack? state "i" "iii")))
           ; Must be in attack phase
           (is-not (-> (create-game 2 [{:tiles [(create-tile "i" :units [(create-unit 2)])
                                                "iii"]}
                                       {:tiles ["ii" "iii"]}]
                                    :turn-phase :movement-phase)
                       (valid-attack? "i" "ii")))
           ; Valid attack
           (is (-> (create-game 2 [{:tiles [(create-tile "i" :units [(create-unit 2)])
                                            "iii"]}
                                   {:tiles ["ii" "iii"]}]
                                :turn-phase :attack-phase)
                   (valid-attack? "i" "ii"))))}
  [state src-name dst-name]
  {:pre [(map? state) (string? src-name) (string? dst-name)]}
  (let [src-tile (get-tile state src-name)
        dst-tile (get-tile state dst-name)]
    (and (= (get-player-id-in-turn state))
         (= (:turn-phase state)
            :attack-phase)
         (not= (:owner-id dst-tile)
               (:owner-id src-tile))
         (> (count-units state src-name) 1)
         (> (count-units state dst-name) 0)
         (neighbors? src-name dst-name))))

(comment

  (defn conquer-tile
    {:test (fn []
             (let [state (-> (create-game 2 [{:tiles [(create-tile "i" :units [(create-unit 2)])]}
                                             {:tiles [(create-tile "ii" :units [(create-unit 0)])]}])
                             (conquer-tile "i" "ii"))]
               (is= (get-in-tile state "ii" :owner-id)
                    "p1")
               (is= (count-units state "i")
                    1)
               (is= (count-units state "ii")
                    1)))}
    [state src-name dst-name]
    {:pre [(>= (count-units state src-name) 2)
           (= (count-units state dst-name) 0)]}
    (let [src-owner-id (get-in-tile state src-name :owner-id)]
      (-> state

          (update-tile src-name :unit-count dec)
          (update-tile dst-name :owner-id src-owner-id)
          (update-tile dst-name :unit-count 1)))))

(comment

  (defn- attack-once
    "Attacks once from src-tile to dst-tile.
    Takes over defending tile if defending tile has 0 units at end of attack.

    50/50 chance for either side to win."
    {:test (fn []
             (let [state (-> (create-game 2 [{:tiles [(create-tile "i" :unit-count 3)]}
                                             {:tiles [(create-tile "ii" :unit-count 3)]}]
                                          :turn-phase :attack-phase)
                             (attack-once "i" "ii"))]
               (is= (map (fn [n] (count-units state n))
                         ["i" "ii"])
                    [3 2]))
             ; Test conquering tile
             (let [state (-> (create-game 2 [{:tiles [(create-tile "i" :unit-count 2)]}
                                             {:tiles [(create-tile "ii" :unit-count 1)]}]
                                          :turn-phase :attack-phase)
                             (attack-once "i" "ii"))]
               (is= (map (fn [n] (count-units state n))
                         ["i" "ii"])
                    [1 1])
               (is= (get-in-tile state "ii" :owner-id)
                    "p1")))}
    [state src-name dst-name]
    {:pre [(map? state) (every? string? [src-name dst-name])]}
    (if-not (valid-attack? state src-name dst-name)
      (error "Invalid attack")
      (let [[state n] (random-int state 2)
            state (if (< n 1)
                    (update-tile state dst-name :unit-count dec)
                    (update-tile state src-name :unit-count dec))]
        (if-not (zero? (count-units state dst-name))
          state
          (conquer-tile state src-name dst-name))))))

(comment

  (defn attack-k-times
    "Attacks k times from the src-tile to the dst-tile."
    {:test (fn []
             (let [state (-> (create-game 2 [{:tiles [(create-tile "i" :unit-count 15)]}
                                             {:tiles [(create-tile "ii" :unit-count 10)]}]
                                          :turn-phase :attack-phase
                                          :seed 2)
                             (attack-k-times "i" "ii" 10))]
               (is= (map (fn [t] (get-in-tile state t :unit-count))
                         ["i" "ii"])
                    [10 5])))}
    [state src-name dst-name k]
    (reduce (fn [state _]
              (if (valid-attack? state src-name dst-name)
                (attack-once state src-name dst-name)
                (reduced state)))
            state
            (range k)))

  (defn attack-with-k
    "Attacks until k units are consumed or the defending tile is taken."
    {:test (fn []
             (let [state (-> (create-game 2 [{:tiles [(create-tile "i" :unit-count 20)]}
                                             {:tiles [(create-tile "ii" :unit-count 20)]}]
                                          :turn-phase :attack-phase
                                          :seed 2)
                             (attack-with-k "i" "ii" 10))]
               (is= (get-in-tile state "i" :unit-count)
                    10)))}
    [state src-name dst-name k]
    (let [orig-unit-count (get-in-tile state src-name :unit-count)]
      (reduce (fn [state _]
                (if (and (valid-attack? state src-name dst-name)
                         (> (get-in-tile state src-name :unit-count)
                            (- orig-unit-count k)))
                  (attack-once state src-name dst-name)
                  (reduced state)))
              state
              (range))))

  (defn attack-till-end
    "Attacks until either side is defeated."
    {:test (fn []
             (let [state (-> (create-game 2 [{:tiles [(create-tile "i" :unit-count 15)]}
                                             {:tiles [(create-tile "ii" :unit-count 10)]}]
                                          :turn-phase :attack-phase
                                          :seed 2)
                             (attack-till-end "i" "ii"))]
               (is= (get-in-tile state "ii" :owner-id)
                    "p1")))}
    [state src-name dst-name]
    (reduce (fn [state _]
              (if (valid-attack? state src-name dst-name)
                (attack-once state src-name dst-name)
                (reduced state)))
            state
            (range))))

(defn valid-move?
  "Determines whether a unit movement is valid.

  1. src and dst belong to the same player.
  2. src owner is in turn.
  3. turn is in movement phase
  4. src and dst have at least one unit remaining at the end of the move."
  ;TODO track unit movements to prevent units from moving more than 1
  ; territory every turn
  {:test (fn []
           (is (-> (create-game 2 [{:tiles [(create-tile "i" :units [(create-unit 2)])
                                            "ii"]}]
                                :turn-phase :movement-phase)
                   (valid-move? "i" "ii" 1))))}
  [state src-name dst-name unit-count]
  {:pre [(map? state) (string? src-name) (string? dst-name) (pos-int? unit-count)]}
  (let [src-tile (get-tile state src-name)
        dst-tile (get-tile state dst-name)]
    (and (= (:owner-id src-tile)
            (:owner-id dst-tile)
            (get-player-id-in-turn state))
         (= (:turn-phase state)
            :movement-phase)
         (pos-int? (- (count-units state src-name)
                      unit-count)))))

(defn move-k-units
  "Moves k units from src to dst."
  {:test (fn []
           ; Valid move
           (let [state (-> (create-game 2 [{:tiles [(create-tile "i" :units [(create-unit 5)])
                                                    "ii"]}]
                                        :turn-phase :movement-phase)
                           (move-k-units "i" "ii" 3))]
             (is= (count-units state "i") 2)
             (is= (count-units state "ii") 4))
           ; Cannot move units to unowned territory
           (error? (-> (create-game 2 [{:tiles [(create-tile "i" :units [(create-unit 3)])
                                                "ii"]}
                                       {:tiles ["iii" "iv"]}])
                       (move-k-units "i" "iii" 1)))
           ; Cannot move more units than in src
           (error? (-> (create-game 2 [{:tiles [(create-tile "i" :units [(create-unit 3)])
                                                "ii"]}])
                       (move-k-units "i" "ii" 10))))}
  [state src-name dst-name k]
  (if-not (valid-move? state src-name dst-name k)
    (error "Invalid move")
    (-> state
        (remove-units src-name k)
        (add-units dst-name k))))

(defn valid-reinforcement?
  "Determines whether a reinforcement is valid."
  ;TODO track total reinforcements in a turn
  {:test (fn []
           ; Tile owner must be in turn
           (is-not (-> (create-game 2 [{} {:tiles ["i"]}]
                                    :turn-phase :reinforcement-phase)
                       (valid-reinforcement? "i" 1)))
           ; Turn must be in reinforcement phase
           (is-not (-> (create-game 2 [{:tiles ["i"]}]
                                    :turn-phase :attack-phase)
                       (valid-reinforcement? "i" 1)))
           ; Valid reinforcement
           (is (-> (create-game 2 [{:tiles [(create-tile "i" :unit-count 2)]}]
                                :turn-phase :reinforcement-phase)
                   (valid-reinforcement? "i" 1))))}
  [state tile-name unit-quantity]
  {:pre [(map? state) (string? tile-name) (pos-int? unit-quantity)]}
  (let [tile (get-tile state tile-name)]
    (and (= (:owner-id tile)
            (get-player-id-in-turn state))
         (= (:turn-phase state)
            :reinforcement-phase))))

(defn reinforce-tile
  "Adds units to a tile."
  {:test (fn []
           (is= (-> (create-game 2 [{:tiles [(create-tile "i" :units [(create-unit 1)])]}]
                                 :turn-phase :reinforcement-phase)
                    (reinforce-tile "i" 10)
                    (count-units "i"))
                11))}
  [state tile-name reinforcement-count]
  (if-not (valid-reinforcement? state tile-name reinforcement-count)
    (error "Invalid reinforcement")
    (add-units state tile-name reinforcement-count)))