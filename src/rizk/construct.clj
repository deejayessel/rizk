(ns rizk.construct)

(defn create-empty-state
  {:test (fn []
           (is= (create-empty-state)
                {:player-in-turn 1
                 :players        {1 {:territories ["Alaska"
                                                   "Alberta"
                                                   "Ontario"]
                                     :cards       {:a 1
                                                   :b 2
                                                   :c 1}}
                                  2 {:territories ["Northwest Territory"
                                                   "Greenland"
                                                   "Quebec"]
                                     :cards       {:a 0
                                                   :b 0
                                                   :c 0}}
                                  3 {:territories ["Western United States"
                                                   "Eastern United States"
                                                   "Central America"]
                                     :cards       {:a 1
                                                   :b 0
                                                   :c 0}}}

                 :map            {"Alaska" {:name        "Alaska"
                                            :owner       1
                                            :troop-count 1}
                                  }
                 :rules          {:initial-army-size          20
                                  :initial-reinforcement-size 5
                                  :initial-card-exchange-rate 4
                                  }
                 }))}
  [])