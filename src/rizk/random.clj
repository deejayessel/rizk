(ns rizk.random
  (:require [ysera.test :refer [is= is is-not error?]]
            [ysera.random :refer [get-random-int
                                  random-nth
                                  shuffle-with-seed]]))

(defn- balanced-partition
  "Partition a collection into n pieces, where the size of any two pieces
  differs by no greater than 1."
  {:test (fn []
           (let [balanced? (fn [partition-list]
                             (as-> (map count partition-list) $
                                   (map - $ (rest $))
                                   (every? (fn [diff] (>= 1 diff -1)) $)))]
             (is (balanced? (balanced-partition 3 (range 20))))
             (is (balanced? (balanced-partition 3 (range 21))))
             (is (balanced? (balanced-partition 3 (range 22))))
             (is (balanced? (balanced-partition 4 (range 3))))
             (is (balanced? (balanced-partition 3 (range 4))))))}
  [n coll]
  {:pre [(pos-int? n) (coll? coll)]}
  (if (= 1 n)
    coll
    (let [coll-size (count coll)
          piece-size (quot coll-size n)
          partition-size (* n piece-size)
          [front back] (split-at partition-size coll)
          parted (partition piece-size front)]
      (if (empty? back)
        parted
        (reduce-kv (fn [parted i rem]
                     (update parted i (fn [piece]
                                        (conj piece rem))))
                   (vec parted)
                   (vec back))))))

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
  {:pre (int? seed) (pos-int? n) (coll? coll)}
  (if (= 1 n)
    coll
    (let [[seed, shuffled-coll] (shuffle-with-seed seed coll)
          partitioned-coll (balanced-partition n shuffled-coll)]
      [seed partitioned-coll])))

(defn roll-n-dice
  "Rolls n dice and gives new seed."
  {:test (fn []
           (is= (roll-n-dice 38 3)
                [[2 1 4] 654490949189288373]))}
  [seed n]
  (reduce (fn [[rolls seed] _]
            (let [[new-seed roll] (get-random-int seed 6)]
              [(conj rolls roll) new-seed]))
          [[] seed]
          (range 0 n)))
