(ns rizk.random-state
  (:require [ysera.test :refer [is is-not is= error?]]
            [rizk.random :as random]
            [rizk.construct :refer [create-game
                                    create-node]]))


(defn roll-n-dice
  "Rolls n dice and returns the state with updated seed along with roll result."
  {:test (fn []
           (let [state (create-game 2)
                 seed (:seed state)
                 [state roll] (roll-n-dice state 1)]
             ; check roll
             (is= roll [4])
             ; check seed is updated in state
             (is (not= (:seed state) seed))))}
  [state n]
  (let [[seed roll] (random/roll-n-dice (:seed state) n)
        state (assoc state :seed seed)]
    [state roll]))

(defn get-random-card
  "Returns the state with updated seed and a random card type."
  {:test (fn []
           (let [state (create-game 2)
                 seed (:seed state)
                 [state card-type] (get-random-card state)]
             ; check card
             (is= card-type :b)
             ;check seed updated in state
             (is (not= (:seed state) seed))
             ))}
  [state]
  (let [[seed card-type] (random/get-random-card (:seed state))
        state (assoc state :seed seed)]
    [state card-type]))