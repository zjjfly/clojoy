(ns clojoy.ch10.atom
  (:require [clojoy.utils.concurrency :as c]))

;atom类似Java中的AtomicXXX类,它的修改是无法回滚的,所以在事务中使用atom要保证对其的修改是幂等的

;使用atom实现一个全局的自增计时器
(def ^:dynamic *time* (atom 0))
(defn tick [] (swap! *time* inc))
(c/dothreads! tick :threads 1000 :times 100)
@*time*
;100000

;使用atom实现记忆函数
(defn manipulate-memoize [f]
  (let [cache (atom {})]
    (with-meta
      (fn [& args]
        (or (second (find @cache args))
            (let [ret (apply f args)]
              (swap! cache assoc args ret)
              ret)))
      {:cache cache})))

(def slowly (fn [x] (Thread/sleep 1000) x))
(time [(slowly 1) (slowly 1)])
;"Elapsed time: 2002.629262 msecs"

(def sometimes-slowly (manipulate-memoize slowly))
(time [(sometimes-slowly 1) (sometimes-slowly 1)])
;"Elapsed time: 1001.381924 msecs"

(meta sometimes-slowly)
; {:cache #object[clojure.lang.Atom 0x7c497dd2 {:status :ready, :val {(1) 1}}]}
;删除缓存
(let [cache (:cache (meta sometimes-slowly))]
  (swap! cache dissoc '(1)))                                ;使用swap!而不是reset!,这样可以避免删除其他缓存
;{}
(meta sometimes-slowly)
;{:cache #object[clojure.lang.Atom 0x7c497dd2 {:status :ready, :val {}}]}

(time [(sometimes-slowly 99) (sometimes-slowly 99)])
;"Elapsed time: 1001.333585 msecs"
;=>[99 99]
