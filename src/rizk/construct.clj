(ns rizk.construct
  (:require [rizk.definitions :refer [get-definitions]]
            [ysera.test :refer [is= is is-not error?]]))

(defn create-empty-state
  {:test (fn []
           (is= (create-empty-state 2)
                {:player-in-turn 1
                 :seed           0
                 :players        {1 {:id          1
                                     :territories []
                                     :cards       {:a 0
                                                   :b 0
                                                   :c 0}}
                                  2 {:id          2
                                     :territories []
                                     :cards       {:a 0
                                                   :b 0
                                                   :c 0}}}
                 :map            {}
                 :rules          {:initial-army-size          20
                                  :initial-reinforcement-size 5
                                  :initial-card-exchange-rate 4}}))}
  [num-players]
  {:player-in-turn 1
   :seed           0
   :players        (->> (range 1 (+ num-players 1))
                        (map-indexed (fn [index player-num]
                                       {:id          player-num
                                        :territories []
                                        :cards       {:a 0 :b 0 :c 0}}))
                        (reduce (fn [a v]
                                  (assoc a (:id v) v))
                                {}))
   :map            {}
   :rules          {:initial-army-size          20
                    :initial-reinforcement-size 5
                    :initial-card-exchange-rate 4}})