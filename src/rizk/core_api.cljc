(ns rizk.core-api
  (:require [ysera.test :refer [is= is is-not error?]]
            [ysera.error :refer [error]]
            [rizk.util :refer [dec-by]]
            [rizk.construct :refer [create-game
                                    create-tile
                                    active-player-id
                                    neighbor-names
                                    get-in-tile
                                    get-owned-groups
                                    get-tile
                                    get-tiles
                                    neighbors?
                                    player-count
                                    player-ids
                                    update-tile
                                    update-turn-phase]]
            [rizk.core :refer []]))

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

(defn go-to-next-turn
  "Moves onto the next player's turn."
  {:test (fn []
           (is= (-> (create-game 2)
                    (go-to-next-turn)
                    (active-player-id))
                "p2")
           (is= (-> (create-game 2)
                    (go-to-next-turn)
                    (go-to-next-turn)
                    (active-player-id))
                "p1"))}
  [state]
  {:pre [(map? state)]}
  (let [active-id (active-player-id state)
        player-ids (player-ids state)
        next-id (->> (concat player-ids player-ids)
                     (drop-while (fn [id] (not= id active-id)))
                     (second))]
    (assoc state :player-in-turn next-id)))