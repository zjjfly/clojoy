(ns clojoy.ch7.a-star
  (:require [clojoy.ch5.vector :refer [neighbors]]))

;实现A*算法
(def world [[1 1 1 1 1]
            [999 999 999 999 1]
            [1 1 1 1 1]
            [1 999 999 999 1]
            [1 1 1 1 1]])
(neighbors 5 [0 0])

(defn estimate-cost
  [step-cost-est size x y]
  (* step-cost-est
     (- (+ size size) x y 2)))
(estimate-cost 900 5 0 0)
;7200
(estimate-cost 900 5 4 4)
;0
(defn path-cost [node-cost cheapest-nbr]
  (+ node-cost
     (:cost cheapest-nbr 0)))
(path-cost 900 {:cost 1})
;901
(defn total-cost [newcost step-cost-est size y x]
  (+ newcost
     (estimate-cost step-cost-est size y x)))
(total-cost 0 900 5 0 0)
;7200
(total-cost (path-cost 900 {:cost 1}) 900 5 4 3)
;1801

(defn min-by [f coll]
  (when (seq coll)
    (reduce (fn [min elem]
              (if (> (f min) (f elem))
                elem
                min))
            coll)))
(min-by :cost [{:cost 100} {:cost 36} {:cost 9}])
;{:cost 9}

(defn a* [start-yx step-est cell-costs]
  (let [size (count cell-costs)]
    (loop [steps 0
           routes (vec (repeat size (vec (repeat size nil))))
           work-todo (sorted-set [0 start-yx])]
      (if (empty? work-todo)
        [(peek (peek routes)) :steps steps]
        (let [[_ yx :as work-item] (first work-todo)
              rest-work-todo (disj work-todo work-item)
              nbr-yxs (neighbors size yx)
              cheapest-nbr (min-by :cost
                                   (keep #(get-in routes %)
                                         nbr-yxs))
              newcost (path-cost (get-in cell-costs yx)
                                 cheapest-nbr)
              oldcost (:cost (get-in routes yx))]
          (if (and oldcost (>= newcost oldcost))
            (recur (inc steps) routes rest-work-todo)
            (recur (inc steps)
                   (assoc-in routes yx
                             {:cost newcost
                              :yxs  (conj (:yxs cheapest-nbr [])
                                          yx)})
                   (into rest-work-todo
                         (map (fn [w]
                                (let [[y x] w]
                                  [(total-cost newcost step-est size y x) (vec w)]))
                              nbr-yxs)))))))))
(a* [0 0]
    900
    world)
;[{:cost 9, :yxs [[0 0] [0 1] [0 2] [0 3] [0 4] [1 4] [2 4] [3 4] [4 4]]} :steps 92]
(a* [0 0]
    900
    [[1 1 1  2    1]
     [1 1 1  1  999]
     [1 1 1  999  1]
     [1 1 1  2    1]
     [1 1 1  666  1]
     ])
