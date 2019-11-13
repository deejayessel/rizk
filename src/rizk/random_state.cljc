(ns rizk.random-state
  (:require [ysera.test :refer [is is-not is= error?]]
            [ysera.random :refer [get-random-int
                                  random-nth
                                  shuffle-with-seed]]
            [rizk.random :as random]
            [rizk.construct :refer [create-game
                                    create-tile]]))


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

(defn random-int
  {:test (fn []
           (let [state (create-game 2)
                 seed (:seed state)
                 [state n] (random-int state 10)]
             ; check int
             (is= n 8)
             ; check seed updated in state
             (is (not= (:seed state) seed))))}
  [state max]
  (let [[seed n] (get-random-int (:seed state) max)
        state (assoc state :seed seed)]
    [state n]))