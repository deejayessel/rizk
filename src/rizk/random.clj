(ns rizk.random
  (:require [rizk.util :refer [floor ceiling]]
            [ysera.test :refer [is= is is-not error?]]
            [ysera.random :refer [random-nth
                                  shuffle-with-seed]]))

(defn random-partition-with-seed
  "Randomly partition a collection into n pieces."
  {:test (fn []
           ; test all pieces have same size
           (is= (random-partition-with-seed 0 3 (range 21))
                [3681289173776380072
                 [[0 2 4 3 15 11 17]
                  [7 13 14 6 18 5 19]
                  [16 12 20 9 1 10 8]]])
           ; test not all pieces have same size
           (is= (random-partition-with-seed 0 3 (range 20))
                [667369761183368783
                 [[14 0 2 12 13 4 18]
                  [1 6 5 3 9 10 11]
                  [16 15 19 17 8 7]]])
           (is= (->> (random-partition-with-seed 0 3 (range 4))
                     (second)
                     (map count))
                [2 1 1]))}
  [seed n coll]
  (let [[seed, shuffled-coll] (shuffle-with-seed seed coll)
        coll-size (count coll)
        piece-size (floor (/ coll-size n))
        partition-size (* n piece-size)
        partitioned-coll (partition piece-size
                                    (take partition-size shuffled-coll))
        leftovers (drop partition-size shuffled-coll)]
    [seed
     (if (empty? leftovers)
       partitioned-coll
       (reduce-kv (fn [partitioned-coll i leftover]
                    (update partitioned-coll i (fn [partition-piece]
                                                 (conj partition-piece leftover))))
                  (vec partitioned-coll)
                  (vec leftovers)))]
    ))


