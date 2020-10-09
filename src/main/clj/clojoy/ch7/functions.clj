(ns clojoy.ch7.functions
  (:require [clojure.test :as test]
            [clojoy.utils.assert :as assert])
  (:import (java.util Date)
           (clojure.lang ArityException)))

;clojure中函数式一等公民
;一等的定义是:
;1.它可以按需创建
;2.它可以在数据结构中存储
;3.它可以作为参数传递给一个函数
;4.它可以作为函数的返回值

;从函数组合也能看出函数是clojure的主要计算单元
(def fifth (comp first rest rest rest rest))

(fifth [1 2 3 4 5])
;5
;构建一个可以返回任意的nth函数的函数,这种函数叫做高阶函数
(defn fnth
  [n]
  (apply comp (cons first
                    (take (dec n) (repeat rest)))))
((fnth 3) [1 2 3 4 5])
;3
;组合的适用的情况:需要一个连续应用一些函数的函数,且要将这个函数返回
(map (comp keyword
           #(.toLowerCase %)
           name)
     '(a b c))
;(:a :b :c)

;偏函数
((partial + 5) 100 200)
;305
;偏函数不是柯里化.偏函数只要给它另一个实参,就会去尝试求值,柯里化的函数则需要所有的实参才会去求值
;因为clojure允许函数有可变数量的实参,柯里化的意义不大

;complement接收一个返回真值的函数,返回一个和原函数的结果相反的函数
[((complement identity) true)
 ((complement identity) 42)
 ((complement identity) false)
 ((complement identity) nil)
 ]
;[false false true true]
((complement even?) 2)
;false

;把函数作为数据的一个例子是clojure.test可以把测试方法定义在函数的元数据中
(defn join
  {:test (fn []
           (assert
             (= (join "," [1 2 3]) "1,3,3")))}
  [sep s]
  (apply str (interpose sep s)))
(test/run-tests)
;定义元数据的其他方法
(defn ^:private ^:dynamic
  sum [nums]
  (map + nums))
(defn ^{:private true :dynamic true}
  sum [nums]
  (map + nums))
(defn sum {:private true :dynamic true} [nums]
  (map + nums))
(defn sum
  ([nums]
   (map + nums))
  {:private true :dynamic true})
(meta #'sum)

;高阶函数
;函数作为参数
;sort
(sort > [7 1 4])
;(7 4 1)
(sort [(Date.) (Date. 100)])
;(#inst"1970-01-01T00:00:00.100-00:00" #inst"2020-08-09T02:42:42.269-00:00") 这种形式叫tagged literal
;sort无法处理需要对元素先进行一些转换再比较的情况
(assert/assert-error ArityException (sort second [[:a 7] [:c 13] [:b 21]]))
;form throws a clojure.lang.ArityException(Wrong number of args (2) passed to: clojure.core/second--5377)
;Wrong number of args (2) passed to: clojure.core/second
;这种情况需要使用sort-by
(sort-by second [[:a 7] [:c 13] [:b 21]])
;([:a 7] [:c 13] [:b 21])
;通过sort-by也可以解决元素的类型不同导致无法比较的情况
(sort-by str ["z" "x" "a" "aa" 1 5 8])
;(1 5 8 "a" "aa" "x" "z")
(sort-by :age [{:age 99} {:age 13} {:age 7}])
;({:age 7} {:age 13} {:age 99})
;使用sort-by和partial构建新的排序函数
(def plays [{:band "Burial", :plays 979, :loved 9}
            {:band "Eno", :plays 2333, :loved 15}
            {:band "Bill Evans", :plays 979, :loved 9}
            {:band "Magma", :plays 2665, :loved 31}])
(def sort-by-loved-ratio (partial sort-by #(/ (:plays %) (:loved %))))
(sort-by-loved-ratio plays)
;({:band "Magma", :plays 2665, :loved 31}
; {:band "Burial", :plays 979, :loved 9}
; {:band "Bill Evans", :plays 979, :loved 9}
; {:band "Eno", :plays 2333, :loved 15})

;函数作为返回值
;实现一个按照map的某些key的值的排序器
(defn columns
  [column-names]
  (fn [row]
    (vec (map row column-names))))
((columns [:plays :loved :band])
 {:band "Burial", :plays 979, :loved 9})
;[979 9 "Burial"]
(sort-by (columns [:plays :loved :band])
         plays)
;({:band "Bill Evans", :plays 979, :loved 9}
; {:band "Burial", :plays 979, :loved 9}
; {:band "Eno", :plays 2333, :loved 15}
; {:band "Magma", :plays 2665, :loved 31})

;named parameter
(defn slope
  [& {:keys [p1 p2] :or {p1 [0 0] p2 [1 1]}}]
  (float (/ (- (p2 1) (p1 1))
            (- (p2 0) (p1 0)))))
(slope :p1 [4 15] :p2 [3 21])
;-6.0
(slope :p2 [2 1])
;0.5
(slope)
;1.0

;函数的前置和后置条件
(defn slope2
  [p1 p2]
  {
   :pre [(not= p1 p2) (vector? p1) (vector? p2)]
   :post [(float? %)]
   }
  (/ (- (p2 1) (p1 1))
     (- (p2 0) (p1 0))))
(assert/assert-error AssertionError (slope2 [10 10] [10 10]))
;form throws a java.lang.AssertionError(Assert failed: (not= p1 p2))
(assert/assert-error AssertionError (slope2 [10 10] '(1 10)))
;form throws a java.lang.AssertionError(Assert failed: (vector? p2))
(assert/assert-error AssertionError (slope2 [10 1] [1 20]))
;form throws a java.lang.AssertionError(Assert failed: (float? %))
(slope2 [10. 1] [1 20])
;-2.111111111111111
;相比于显式的assert,这种方式是clojure提供的机制,更加符合标准,而且它可以不和特定的函数体绑定
(defn put-things [m]
  (into m {:meat "beef" :veggie "broccoli"}))
(put-things {})
;{:meat "beef", :veggie "broccoli"}

(defn vegan-constraints
  [f m]
  {:pre [(:veggie m)]
   :post [(:veggie %) (nil? (:meat %))]}
  (f m))
(assert/assert-error AssertionError
                     (vegan-constraints put-things {:veggie "carrot"}))
;form throws a java.lang.AssertionError(Assert failed: (nil? (:meat %)))

(defn balanced-diet
  [f m]
  {:post [(:veggie %) (:meat %)]}
  (f m))
(balanced-diet put-things {})
;{:meat "beef", :veggie "broccoli"}
