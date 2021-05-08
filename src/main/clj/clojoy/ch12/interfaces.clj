(ns clojoy.ch12.interfaces
  (:import [java.util ArrayList Collections HashMap Map]
           [java.util.concurrent FutureTask]
           (java.awt Point))
  (:require [clojoy.utils.assert :as a]))

;clojure的函数实现了很多接口,其中有些是在和Java进行互操作的时候非常有用:
;1.Callable
;2.Comparator
;3.Runnable

;clojure的函数的返回值只要满足Comparator的规定,就可以作为比较器使用
;以实现对list从大到小排序为例
(defn gimme [] (ArrayList. [1 3 4 8 2]))
;Java的方式
(doto (gimme)
  (Collections/sort (Collections/reverseOrder)))
;[8 4 3 2 1]
;clojure的方式
(doto (gimme)
  (Collections/sort >))
;[8 4 3 2 1]

;clojure可以作为参数传递给Thread的构造函数
(doto (Thread. #(do (Thread/sleep 1000)
                    (println "haikebaa!")))
  .start)
;haikebaa!

;clojure函数也可以FutureTask一起使用
(let [f (FutureTask. #(do (Thread/sleep 1000) 1))]
  (.start (Thread. #(.run f)))
  (.get f))
;1

;clojure的集合也可以被Java的API使用
;clojure的顺序集合符合Java的java.util.List接口的不可变部分(即那些不会改变list中元素的操作)
(.get [1 2 3] 1)
;2
(.get (repeat :a) 2)
;:a
(.containsAll [1 2 3] [2 3])
;true
;对于会修改集合的操作,会报错
(a/assert-error UnsupportedOperationException (.add [1 2 3] 4))
;form throws a java.lang.UnsupportedOperationException().
;这种情况最好是使用clojure的函数

;clojure的vector是唯一实现了Comparable接口的clojure集合,所以可以进行比较
(.compareTo [:a] [:a])
;0
(.compareTo [:a :b] [:a])
;1
(.compareTo [:a :b] [:a :b :c])
;-1
(sort [[:a :b :c] [:a] [:a :b]])
;([:a] [:a :b] [:a :b :c])

;clojure的vector是唯一实现了RandomAccess接口的clojure集合,所以可以支持对其中元素的常量时间的访问
(.get [1 2 3] 2)

;clojure的集合都实现了Collection接口,所以可以把它们转成具体的Java集合类型,然后调用Java现有的API
;以为一个集合洗牌为例
(defn shuffle [coll]
  (seq (doto (ArrayList. coll)
         Collections/shuffle)))
(shuffle (range 10))
;(2 9 4 6 1 8 3 5 7 0)

;clojure的map实现了java.util.Map接口的不可变部分
(def ^Map m (into {} (doto (HashMap.) (.put :a 1))))
(.get m :a)
;1
;不能调用Java的那些会修改Map的方法
(a/assert-error UnsupportedOperationException (.put m :a 2))
;form throws a java.lang.UnsupportedOperationException().

;无论是clojure还是Java的set,如果其中的元素是可变类型,会出现奇怪的问题
(def x (Point. 0 0))
(def y (Point. 0 2))
(def points #{x y})
points
;#{#object[java.awt.Point 0x2a6e43c3 "java.awt.Point[x=0,y=0]"]
;  #object[java.awt.Point 0x72c53210 "java.awt.Point[x=0,y=2]"]}
;把y的位置改成和x一样,但points中还是有两个point
(.setLocation y 0 0)
points
;#{#object[java.awt.Point 0x2a6e43c3 "java.awt.Point[x=0,y=0]"]
;  #object[java.awt.Point 0x72c53210 "java.awt.Point[x=0,y=0]"]}

;definterface可以动态的生成Java接口,引入它的原因是为了方法的参数和返回支持原生类型的类型提示
;最新的clojure版本中,普通函数也支持原生类型的类型提示,所以尽量不要再使用definterface,而是首选defprotocol
(definterface ISliceable
  (slice [^long s ^long e])
  (^long sliceCount []))
(defprotocol Sliceable
  (slice [this ^long s ^long e])
  (^long sliceCount [this]))

