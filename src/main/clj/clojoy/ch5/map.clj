(ns clojoy.ch5.map
  (:require
   [clojoy.utils.assert :as assert]))

;clojure中的map用于存储带名的值,最常见的是hash map,map字面量产生的就是hash map,它是无序的
;另一种生成hash map的方法
(hash-map :a 1, :b 2, :c 3, :d 4, :e 5)
;{:e 5, :c 3, :b 2, :d 4, :a 1}

;clojure的map支持任何类型的key,同一个map中的不同key的类型也可以使不同的
(let [m {:a 1, 1 :b, [1 2 3] "4 5 6"}]
  [(get m :a) (get m [1 2 3])])
;[1 "4 5 6"]

;clojure的map和其他集合一样也可以作为函数,它接受一个key,返回这个key对应的值,类似上面的get函数
(let [m {:a 1, 1 :b, [1 2 3] "4 5 6"}]
  [(m :a) (m [1 2 3])])
;[1 "4 5 6"]

;把map传给seq函数,返回的是map的条目的序列
(seq {:a 1, :b 2})
;([:a 1] [:b 2])

;这个条目的序列反过来可以用于构建map
(into {} [[:a 1] [:b 2]])
;{:a 1, :b 2}

;即使键值对不是存在vector中,也可以用于构建map
(into {} (map vec '[(:a 1) (:b 2)]))
;{:a 1, :b 2}

;你的键值对也不需要分好组,可以使用apply
(apply hash-map [:a 1 :b 2])
;{:b 2, :a 1}
;这种做法对于函数sorted-map和array-map也有用

;还有一种构建map的方法是zipmap,它接受两个序列,第一个是键的集合,第二个是值的集合
(zipmap [:a :b] [1 2])
;{:a 1, :b 2}

;clojure中提供了有序的map,使用sorted-map或sorted-map-by来生成,会根据key的比较结果来排序
(sorted-map :thx 1138 :r2d 2)
;{:r2d 2, :thx 1138}
(sorted-map-by #(compare (subs %1 1) (subs %2 1)) "bac" 2 "abc" 9)
;{"bac" 2, "abc" 9}
;和sorted-set一样,如果sorted-map传入的key是不能比较的,会报错
(assert/assert-error ClassCastException
                     (sorted-map :a 1 "b" 2))
;form throws a java.lang.ClassCastException(clojure.lang.Keyword cannot be cast to java.lang.String)

;可以使用subseq和rsubseq来截取sorted map中某个key之后或之前的元素,它们也适用于sorted set
;这个key不一定要存在,subseq会找到最接近它的key
(subseq (sorted-map :a 1 :b 2 :c 3) >= :b)
;([:c 3])
(subseq (sorted-set 1 2 3 5) >= 4)
;(5)

;hash map和sorted map对待数字类型的key是不一样的
;hash map认为不同数字类型的key是不一样的
(assoc {1 :int} 1.0 :float)
;{1 :int, 1.0 :float}
;而sorted map则把它们看成是一样的
(assoc (sorted-map 1 :int) 1.0 :float)
;{1 :float}
;其中原因和sorted-map中使用的默认的比较器有关,它和sorted-set的默认比较器是同一个
;sorted map的使用场景主要是当你需要保证map中的key是按照某种顺序排列的

;如果保持插入的顺序,使用array map
(seq (hash-map :a 1, :b 2, :c 3))
;([:c 3] [:b 2] [:a 1])
(seq (array-map :a 1, :b 2, :c 3))
;([:a 1] [:b 2] [:c 3])

;实现一个函数来得到某个值在集合中对应的所有的key,这个key在不同集合中含义不同
(defn pos [coll pred]
  (cond
    (map? coll) (for [[k val] coll :when (pred val)] k)
    (vector? coll) (pos (zipmap (iterate inc 0) coll) pred)
    (set? coll) (pos (seq coll) pred)
    (seq? coll) (pos (zipmap (iterate inc 0) coll) pred)
    (list? coll) (pos (zipmap (iterate inc 0) coll) pred)
    :default nil))
(pos [:a 1 :b 2 :c 3 :d 4] #(= 3 %))
;(5)
(pos {:a 1, :b 2, :c 3, :d 4} #(= 3 %))
;(:c)
(pos [:a 3 :b 3 :c 3 :d 4] #(= 3 %))
;(1 3 5)
(pos {:a 3, :b 3, :c 3, :d 4} #(= 3 %))
;(:a :b :c)
(pos {:a 1 :b 2 :c 3 :d 4} #{3 4})
;(:c :d)
(pos [2 3 6 7] even?)
;(0 2)
