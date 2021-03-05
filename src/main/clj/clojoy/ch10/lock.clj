(ns clojoy.ch10.lock
  (:refer-clojure :exclude [aget aset count seq])
  (:require [clojure.core :as clj]
            [clojoy.utils.concurrency :as c])
  (:import (java.util.concurrent.locks ReentrantLock)))

(defprotocol SafeArray
  (aset [this i f])
  (aget [this i])
  (count [this])
  (seq [this]))

(defn make-dumb-array [t sz]
  (let [a (make-array t sz)]
    (reify SafeArray
      (count [_] (clj/count a))
      (seq [_] (clj/seq a))
      (aget [_ i] (clj/aget a i))
      (aset [this i f] (clj/aset a
                                 i
                                 (f (aget this i)))))))

;上面的实现由线程安全问题
(defn pummel [a]
  (c/dothreads! #(dotimes [i (count a)]
                   (aset a i inc))
                :threads 100))
(def D (make-dumb-array Integer/TYPE 8))
(pummel D)
(seq D)
;(83 82 82 61 82 81 80 79),不是期望的全是100,可以看出有race condition

;使用locking为数组加锁,实现一个线程安全的数组,而且它是可重入的
(defn make-safe-array [t sz]
  (let [a (make-array t sz)]
    (reify SafeArray
      (count [_] (clj/count a))
      (seq [_] (clj/seq a))
      (aget [_ i] (locking a
                    (clj/aget a i)))
      (aset [this i f] (locking a
                         (clj/aset a
                                   i
                                   (f (aget this i))))))))

(def A (make-safe-array Integer/TYPE 8))
(pummel A)
(seq A)
;(100 100 100 100 100 100 100 100)

;上面的实现的性能不是很好,因为它对整个数组加锁,而不是对单个槽加锁
;可以使用ReentrantLock实现一个更好的实现
(defn lock-i
  "计算某个index落在哪个锁上"
  [target-index num-locks]
  (mod target-index num-locks))

(defn make-smart-array [t sz]
  (let [a (make-array t sz)
        Lsz (/ sz 2)
        L (into-array (take Lsz (repeatedly #(ReentrantLock.))))]
    (reify SafeArray
      (count [_] (clj/count a))
      (seq [_] (clj/seq a))
      (aget [_ i]
        (let [lk (clj/aget L (lock-i (inc i) Lsz))]
          (.lock lk)
          (try
            (clj/aget a i)
            (finally (.unlock lk)))))
      (aset [this i f]
        (let [lk (clj/aget L (lock-i (inc i) Lsz))]
          (.lock lk)
          (try
            (clj/aset a
                      i
                      (f (aget this i)))
            (finally (.unlock lk))))))))
(def S (make-smart-array Integer/TYPE 8))
(pummel S)
(seq S)
; (100 100 100 100 100 100 100 100)
