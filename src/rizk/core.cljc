(ns rizk.core
  (:require [ysera.test :refer [is= is is-not error?]]
            [ysera.error :refer [error]]
            [rizk.util :refer [non-neg-int?]]
            [rizk.random-state :refer [random-int]]
            [rizk.definitions :refer [get-all-tile-defns
                                      get-in-defn
                                      get-tile-defn
                                      get-group-defn]]
            [rizk.construct :refer [active-player-id
                                    add-tiles
                                    create-game
                                    create-tile
                                    get-in-tile
                                    get-owned-groups
                                    get-tile
                                    get-tiles
                                    neighbor-names
                                    neighbors?
                                    troop-count
                                    update-tile]]))

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
        group-bonus (player-reinforcement-bonus state player-id)]
    (+ (max 3 (quot tile-count 3))
       group-bonus)))

(defn valid-attack?
  "Checks if a move is a valid attack.

  1. Owner of src is in turn.
  2. Owner of src does not own dst.
  3. Src tile has 2 or more troops.
  4. Dst tile has 1 or more troops.
  5. Src and Dest are neighbors."
  {:test (fn []
           (let [state (create-game 2 [{:tiles ["i" "iii"]}
                                       {:tiles ["ii" "iii"]}]
                                    :turn-phase :attack-phase)]

             ; Must attack from friendly tile
             (is-not (valid-attack? state "ii" "iii"))
             ; Must attack unfriendly tile
             (is-not (valid-attack? state "i" "iii"))
             ; Must have more than one troop in src
             (is-not (valid-attack? state "iii" "ii"))
             ; Must have some troops in dst
             (is-not (-> (update-tile state "ii" :troop-count dec)
                         (valid-attack? "iii" "ii")))
             ; Territories must be neighbors
             (is-not (valid-attack? state "i" "iii")))

           ; Must be in attack phase
           (is-not (-> (create-game 2 [{:tiles [(create-tile "i" :troop-count 2)
                                                "iii"]}
                                       {:tiles ["ii" "iii"]}]
                                    :turn-phase :movement-phase)
                       (valid-attack? "i" "ii")))

           ; Valid attack
           (is (-> (create-game 2 [{:tiles [(create-tile "i" :troop-count 10)
                                            "iii"]}
                                   {:tiles ["ii" "iii"]}]
                                :turn-phase :attack-phase)
                   (valid-attack? "i" "ii"))))}
  [state src-name dst-name]
  {:pre [(map? state) (string? src-name) (string? dst-name)]}
  (let [src-tile (get-tile state src-name)
        dst-tile (get-tile state dst-name)]
    (and (= (active-player-id state)
            (:owner-id src-tile))
         (= (:turn-phase state)
            :attack-phase)
         (not= (:owner-id dst-tile)
               (:owner-id src-tile))
         (> (:troop-count src-tile) 1)
         (> (:troop-count dst-tile) 0)
         (neighbors? src-name dst-name))))

(defn conquer-tile
  {:test (fn []
           (let [state (-> (create-game 2 [{:tiles [(create-tile "i" :troop-count 2)]}
                                           {:tiles [(create-tile "ii" :troop-count 0)]}])
                           (conquer-tile "i" "ii"))]
             (is= (get-in-tile state "ii" :owner-id)
                  "p1")
             (is= (troop-count state "i")
                  1)
             (is= (troop-count state "ii")
                  1)))}
  [state src-name dst-name]
  {:pre [(>= (troop-count state src-name) 2)
         (= (troop-count state dst-name) 0)]}
  (let [src-owner-id (get-in-tile state src-name :owner-id)]
    (-> state
        (update-tile src-name :troop-count dec)
        (update-tile dst-name :owner-id src-owner-id)
        (update-tile dst-name :troop-count 1))))

(defn- attack-once
  "Attacks once from src-tile to dst-tile.
  Takes over defending tile if defending tile has 0 troops at end of attack.

  50/50 chance for either side to win."
  {:test (fn []
           (let [state (-> (create-game 2 [{:tiles [(create-tile "i" :troop-count 3)]}
                                           {:tiles [(create-tile "ii" :troop-count 3)]}]
                                        :turn-phase :attack-phase)
                           (attack-once "i" "ii"))]
             (is= (map (fn [n] (troop-count state n))
                       ["i" "ii"])
                  [3 2]))
           ; Test conquering tile
           (let [state (-> (create-game 2 [{:tiles [(create-tile "i" :troop-count 2)]}
                                           {:tiles [(create-tile "ii" :troop-count 1)]}]
                                        :turn-phase :attack-phase)
                           (attack-once "i" "ii"))]
             (is= (map (fn [n] (troop-count state n))
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
                  (update-tile state dst-name :troop-count dec)
                  (update-tile state src-name :troop-count dec))]
      (if-not (zero? (troop-count state dst-name))
        state
        (conquer-tile state src-name dst-name)))))

(defn attack-k-times
  "Attacks k times from the src-tile to the dst-tile."
  {:test (fn []
           (let [state (-> (create-game 2 [{:tiles [(create-tile "i" :troop-count 15)]}
                                           {:tiles [(create-tile "ii" :troop-count 10)]}]
                                        :turn-phase :attack-phase
                                        :seed 2)
                           (attack-k-times "i" "ii" 10))]
             (is= (map (fn [t] (get-in-tile state t :troop-count))
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
           (let [state (-> (create-game 2 [{:tiles [(create-tile "i" :troop-count 20)]}
                                           {:tiles [(create-tile "ii" :troop-count 20)]}]
                                        :turn-phase :attack-phase
                                        :seed 2)
                           (attack-with-k "i" "ii" 10))]
             (is= (get-in-tile state "i" :troop-count)
                  10)))}
  [state src-name dst-name k]
  (let [orig-troop-count (get-in-tile state src-name :troop-count)]
    (reduce (fn [state _]
              (if (and (valid-attack? state src-name dst-name)
                       (> (get-in-tile state src-name :troop-count)
                          (- orig-troop-count k)))
                (attack-once state src-name dst-name)
                (reduced state)))
            state
            (range))))

(defn attack-till-end
  "Attacks until either side is defeated."
  {:test (fn []
           (let [state (-> (create-game 2 [{:tiles [(create-tile "i" :troop-count 15)]}
                                           {:tiles [(create-tile "ii" :troop-count 10)]}]
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
          (range)))

(defn valid-move?
  "Determines whether a troop movement is valid.

  1. src and dst belong to the same player.
  2. src owner is in turn.
  3. turn is in movement phase
  3. src and dst have at least one troop remaining at the end of the move."
  ;TODO track troop movements to prevent units from moving more than 1
  ; territory every turn
  {:test (fn []
           (is (-> (create-game 2 [{:tiles [(create-tile "i" :troop-count 2)
                                            "ii"]}]
                                :turn-phase :movement-phase)
                   (valid-move? "i" "ii" 1))))}
  [state src-name dst-name troop-quantity]
  {:pre [(map? state) (string? src-name) (string? dst-name) (pos-int? troop-quantity)]}
  (let [src-tile (get-tile state src-name)
        dst-tile (get-tile state dst-name)]
    (and (= (:owner-id src-tile)
            (:owner-id dst-tile)
            (active-player-id state))
         (= (:turn-phase state)
            :movement-phase)
         (pos-int? (- (troop-count state src-name)
                      troop-quantity)))))

(defn move-k-troops
  "Moves k troops from src to dst."
  {:test (fn []
           (let [state (-> (create-game 2 [{:tiles [(create-tile "i" :troop-count 5)
                                                    "ii"]}]
                                        :turn-phase :movement-phase)
                           (move-k-troops "i" "ii" 3))]
             (is= (troop-count state "i") 2)
             (is= (troop-count state "ii") 4)))}
  [state src-name dst-name k]
  (if-not (valid-move? state src-name dst-name k)
    (error "Invalid move")
    (-> state
        (update-tile src-name :troop-count (fn [t] (- t k)))
        (update-tile dst-name :troop-count (fn [t] (+ t k))))))