(ns clojoy.ch15.transient)

;暂态(transient)可以把clojure的不可变数据结构变成可变数据结构,由此获得性能提升

;使用传统方式实现一个类似concat的函数
(defn zencat1 [x y]
  (loop [src y
         ret x]
    (if (seq src)
      (recur (next src) (conj ret (first src)))
      ret)))
(zencat1 [1 2 3] [4 5 6])
(time (dotimes [_ 1000000]
        (zencat1 [1 2 3] [4 5 6])))
;"Elapsed time: 218.937425 msecs"

;使用暂态实现
(defn zencat2 [x y]
  (loop [src y
         ret (transient x)]
    (if (seq src)
      (recur (next src) (conj! ret (first src)))
      (persistent! ret))))
(time (dotimes [_ 1000000]
        (zencat2 [1 2 3] [4 5 6])))
;"Elapsed time: 203.09752 msecs"

;会发现两个函数的性能没多大区别,甚至用了暂态的还更慢
;这是因为我们输入的vector太小了导致zencat2的性能会受到调用transient和persistent!影响
;所以构建一个大的vector
(def bv (vec (range 1e6)))
(first (time (zencat1 bv bv)))
;"Elapsed time: 110.353302 msecs"
(first (time (zencat2 bv bv)))
;"Elapsed time: 78.841738 msecs"

;要注意,clojure不允许多线程并发地修改暂态,所以不要忘记在函数返回之前用persistent!把暂态转成持久化结构
