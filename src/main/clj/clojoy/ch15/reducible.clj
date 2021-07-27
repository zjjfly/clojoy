(ns clojoy.ch15.reducible
  (:require [criterium.core :as crit]
            [clojure.core.reducers :as r]
            [clojure.core.protocols :as protos]))

;自己构建一个range抽象
(defn empty-range? [start end step]
  (or (and (>= start end) (pos? step))
      (and (<= start end) (neg? step))))

(defn lazy-range [i end step]
  (lazy-seq
   (if (empty-range? i end step)
     nil
     (cons i
           (lazy-range (+ i step)
                       end
                       step)))))

;lazy-range返回的是惰性序列
(lazy-range 5 10 2)
;(5 7 9)

;lazy-range可以用于构建其他的数据结构
(reduce conj [] (lazy-range 6 0 -1))
;[6 5 4 3 2 1]

;lazy-range的reducible的生成器
(defn reducible-range [start end step]
  (fn [reducing-fn init]
    (loop [result init
           i start]
      (if (empty-range? i end step)
        result
        (recur (reducing-fn result i)
               (+ i step))))))

(def countdown-reducible (reducible-range 6 0 -1))

(countdown-reducible conj [])
;[6 5 4 3 2 1]

;transformer,把一个reduce函数转成另一个reduce函数
(defn half [x]
  (/ x 2))
(half 4)
;2
(half 7)
;7/2
(defn half-transformer [f1]
  (fn f1-half [result input]
    (f1 result (half input))))
((reducible-range 0 10 2) (half-transformer +) 0)
;10
((reducible-range 0 10 2) (half-transformer conj) [])
;[0 1 2 3 4]

;half-transformer中的half函数依然是硬编码的,考虑使用下面的方式
(defn mapping [map-fn]
  (fn map-transformer [f1]
    (fn [result input]
      (f1 result (map-fn input)))))
;这种函数被称为transformer的构造函数
((reducible-range 0 10 2) ((mapping half) +) 0)
;10
((reducible-range 0 10 2) ((mapping half) conj) [])
;[0 1 2 3 4]
((reducible-range 0 10 2) ((mapping list) conj) [])
;[(0) (2) (4) (6) (8)]

;实现filter
(defn filtering [filter-pred]
  (fn [f1]
    (fn [result input]
      (if (filter-pred input)
        (f1 result input)
        result))))
((reducible-range 0 10 2) ((filtering #(not= % 2)) +) 0)
;18
((reducible-range 0 10 2) ((filtering #(not= % 2)) conj) [])
;[0 4 6 8]
;把mapping和filtering结合起来
((reducible-range 0 10 2) ((filtering #(not= % 2)) ((mapping half) conj)) [])
;[0 2 3 4]
((reducible-range 0 10 2) ((mapping half) ((filtering #(not= % 2)) conj)) [])
;[0 1 3 4]

;实现mapcatting
(defn mapcatting [map-fn]
  (fn [f1]
    (fn [result input]
      (let [reducible (map-fn input)]
        (reducible f1 result)))))
(defn and-plus-ten [x]
  (reducible-range x (+ x 11) 10))
((and-plus-ten 5) conj [])
;[5 15]
((reducible-range 0 10 2) ((mapcatting and-plus-ten) conj) [])
;[0 10 2 12 4 14 6 16 8 18]

;对filtering和mapping进行封装,使其和clojure原生的map和filter有相似的用法
(defn r-map [mapping-fn reducible]
  (fn new-reducible [reduce-fn init]
    (reducible ((mapping mapping-fn) reduce-fn) init)))
(defn r-filter [filter-pred reducible]
  (fn new-reducible [reduce-fn init]
    (reducible ((filtering filter-pred) reduce-fn) init)))

(def our-final-reducible
  (r-filter #(not= % 2)
            (r-map half
                   (reducible-range 0 10 2))))
(our-final-reducible conj [])
;[0 1 3 4]

;对比一下clojure的reduce和自己实现的reducible的性能
(crit/bench
 (reduce + 0
         (filter even?
                 (map half
                      (lazy-range 0 (* 10 1000 1000) 2)))))
(crit/bench
  ((r-filter even? (r-map half
                          (reducible-range 0 (* 10 1000 1000) 2)))
   + 0))

;通过clojure.core.reducers,让reducible和clojure的reduce进行适配
(defn core-r-map [mapping-fn core-reducible]
  (r/reducer core-reducible (mapping mapping-fn)))
(defn core-r-filter [filter-pred core-reducible]
  (r/reducer core-reducible (filtering filter-pred)))
(reduce conj []
        (core-r-filter #(not= % 2)
                       (core-r-map half [0 2 4 6 8])))
;[0 1 3 4]

(defn reduce-range [reducing-fn init start end step]
  (loop [result init
         i start]
    (if (empty-range? i end step)
      result
      (recur (reducing-fn result i)
             (+ i step)))))

(defn core-reducible-range [start end step]
  (reify protos/CollReduce
    (coll-reduce [this reducing-fn init]
      (reduce-range reducing-fn init start end step))
    (coll-reduce [this reducing-fn]
      (if (empty-range? start end step)
        (reducing-fn)
        (reduce-range reducing-fn start (+ start step) end step)))))
(reduce conj []
        (core-r-filter #(not= % 2)
                (core-r-map half
                     (core-reducible-range 0 10 2))))
;[0 1 3 4]
(reduce + (core-reducible-range 10 12 1))
;21

;fold和reduce类似,但它不需要按序执行和reduce函数有关联性
(r/fold + [1 2 3 4 5])
;15
;让reducible和fold进行适配
(defn core-f-map [mapping-fn core-reducible]
  (r/folder core-reducible (mapping mapping-fn)))
(defn core-f-filter [filter-pred core-reducible]
  (r/folder core-reducible (filtering filter-pred)))

(r/fold +
        (core-f-filter #(not= % 2)
                       (core-f-map half [0 2 4 6 8])))
;实际上clojure的reducers库中已经实现了core-f-map和core-f-filter
(r/fold +
        (r/filter #(not= % 2)
                       (r/map half [0 2 4 6 8])))
;8

;fold有两个参数,它的一个可选的函数参数是聚合函数
;它没有初始值,但可以通过没有参数的reducing函数来实现
(r/fold (fn ([] 100) ([a b] (+ a b))) (range 10))
;这种写法很繁琐,可以使用monoid代替
(r/fold (r/monoid + (constantly 100)) (range 10))
;145

;fold会把集合分成多个片区(默认一个片区512个元素,可以通过传入参数指定)
;不同片区使用reducing函数处理,然后使用聚合函数合并不同片区的返回结果
(r/fold 10 (r/monoid + (constantly 100)) + (range 100))
;5050

;foldcat可以把不同片区的结果聚合到一个集合中.
(r/foldcat (r/filter even? (vec (range 10))))               ;使用vec是因为并行的fold只对vector,map和Cat对象开启
;[0 2 4 6 8]

;测试fold的性能
(def big-vector (vec (range 0 (* 10 1000 1000) 2)))
(crit/bench
  (r/fold + (core-f-filter even? (core-f-map half big-vector))))
