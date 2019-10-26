(ns rizk.core-api
  (:require [ysera.test :refer [is= is is-not error?]]
            [ysera.error :refer [error]]
            [rizk.random :refer [get-random-card]]
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
                                    update-turn-phase
                                    update-seed]]
            [rizk.core :refer [can-draw-card?]]))

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
    (let [[seed card-type] (get-random-card (:seed state))]
      (-> (update-seed state seed)
          (add-card player-id card-type)))))

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