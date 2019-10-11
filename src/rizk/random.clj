(ns rizk.random
  (:require [rizk.definitions :refer [get-definition]]
            [rizk.construct :refer [create-empty-state]]
            [rizk.util :refer [ceiling]]
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
                 [[0 2 12 13 4 18 6]
                  [5 3 9 10 11 16 15]
                  [19 17 8 7 14 1]]])
           )}
  [seed n coll]
  (let [[seed, shuffled-coll] (shuffle-with-seed seed coll)
        coll-size (count coll)
        piece-size (ceiling (/ coll-size n))]
    [seed (partition-all piece-size shuffled-coll)]))
