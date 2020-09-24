(ns com.github.zjjfly.clojoy.ch6.laziness
  (:require
    [com.github.zjjfly.clojoy.utils.assert :as assert]))

;clojure的惰性体现在处理序列类型的方式

;很多语言都或多或少有一些惰性的元素,如Java的&&和||
;使用clojure实现Java的&&
(defn if-chain [x y z]
  (if x
    (if y (if z
            (do
              (println "Made it!")
              :all-truthy)))))
(if-chain () 42 true)
;Made it!
;=>:all-truthy
(if-chain true true false)
;nil
;使用and实现Java的&&
(defn and-chain
  [x y z]
  (and x y z
       (do (println "Made it!")
           :all-truthy)))
(and-chain () 42 true)
;Made it!
;=>:all-truthy
(and-chain true true false)
;false

;实现一个steps函数,可以从一个序列生成一个嵌套结构,效果如下:
;(steps [1 2 3 4])
;=> [1 [2 [3 [4 []]]]]
;首先想到的是用递归
(defn rec-step [[x & xs]]
  (if x
    [x (rec-step xs)] []))
(rec-step [1 2 3 4])
;=>[1 [2 [3 [4 []]]]]
;试一个大一点的序列
(assert/assert-error StackOverflowError (rec-step (range 200000)))
;form throws a java.lang.StackOverflowError()

;使用clojure的lazy-seq可以避免栈溢出问题,使用它有四个注意点:
;1.使用lazy-seq在产生序列的表达式的最外层
;2.如果碰巧需要在操作的时候需要消费另一个序列,使用rest而不是next
;3.处理序列的时候优先使用高阶函数
;4.不要持有head,这会造成GC无法回收这个序列

;第三点会在第七章讨论,现在先看第二点
;使用iterate构建lazy seq,然后分别使用rest和next返回前三个值
(def very-lazy (-> (iterate #(do (print \.) (inc %)) 1)
                   rest rest rest))
;..=> #'com.github.zjjfly.clojoy.ch6.laziness/very-lazy
(def less-lazy (-> (iterate #(do (print \.) (inc %)) 1)
                   next next next))
;..=> #'com.github.zjjfly.clojoy.ch6.laziness/less-lazy
(println (first very-lazy))
;.4
(println (first less-lazy))
;.4
;可以看出,它们结果是一样的,这个书中说的不一样,应该是新版的clojure做了修改
;所以,第二点可以忽略了

;写一个使用lazy seq实现的rec-step
(defn lz-rec-step
  [s]
  (lazy-seq
    (if (seq s)
      (cons (first s) [(lz-rec-step (rest s))])
      [])))
(lz-rec-step [1 2 3 4])
;(1 (2 (3 (4 ()))))
(class (lz-rec-step [1 2 3 4]))
;clojure.lang.LazySeq
(dorun (lz-rec-step (range 200000)))
;nil

;一个简单一点的例子
(defn simple-range
  [i limit]
  (lazy-seq
    (when (< i limit)
      (cons i (simple-range (inc i) limit)))))
(simple-range 1 10)
;(1 2 3 4 5 6 7 8 9)

;根据第四点,不要在函数中持有lazy seq的head不放,这样会阻止GC回收这个lazy seq
;最简单的持有lazy seq的head的方式是把它绑定到本地变量,通过let或binding
(comment (let [r (range 1e9)]
           (first r)
           (last r)))
;999999999
;上面这种情况没有问题,是因为clojure的编译器会推断出r的第一个元素在(first r)之后是不需要的,所以它可以在(last r)执行的时候被回收,而且r最后一个元素之前的所有元素也都可以被回收
;下面的代码会抛出java.lang.OutOfMemoryError,因为第一个元素会在(last r)之后被使用,所以lazy seq中的所有元素都不能被回收,
;(let [r (range 1e9)]
;  (last r)
;  (first r))
;为什么clojure编译器不交换这两个操作?因为clojure无法判断这两个操作都是纯函数,也就无法确定交换它们的顺序不会对程序产生影响

;因为clojure的序列是lazy的,所以序列有可能是无限长的
;clojure提供了一些产生和操作无限长序列的函数
(def x (iterate (fn [n] (/ n 2)) 1))
(take 3 x)
;(1 1/2 1/4)
;一个三角数序列的例子
(defn triangle [n]
  (/ (* n (+ n 1)) 2))
(triangle 10)
;55
;获取序列中的前十个数
(map triangle (range 1 11))
;(1 3 6 10 15 21 28 36 45 55)
;上面的这种实现没有错,但是不够灵活,下面是使用无限长序列的例子
(def tri-nums (map triangle (iterate inc 1)))
;这个tri-nums是一个包含了所有三角数的无限长序列
(take 10 tri-nums)
;(1 3 6 10 15 21 28 36 45 55)
(take 10 (filter even? tri-nums))
;(6 10 28 36 66 78 120 136 190 210)
(nth tri-nums 99)
;5050
(double (reduce + (take 1000 (map / tri-nums))))
;1.998001998001998
(take 2 (drop-while #(< % 10000) tri-nums))
;上面使用的map,reduce,filter都能保持序列的laziness

;虽然clojure的序列大多是lazy的,但clojure本身不是
;因为表达式在传入函数之前就会被求值,而不是在需要的时候求值
;但clojure也提供了按需调用的语义,其中最主要的就是macro,这会在之后讨论
;现在先讨论两个宏delay和force,使用它们可以实现explicit laziness
;delay可以推迟一个表达式的求值,直到使用force显示强制它求值
(defn defer-expensive [cheap expensive]
  (if-let [good-enough (force cheap)]
    good-enough
    (force expensive)))
(defer-expensive (delay :cheap)
                 (delay (do (Thread/sleep 5000) :expensive)))
;:cheap
(defer-expensive (delay false)
                 (delay (do (Thread/sleep 5000) :expensive)))
;:expensive
;delay?可以检测是否是延迟的计算
(delay? (delay (+ 1 1)))
;true
;delay会把计算结果缓存起来,所以表达式只会执行一次,这点和memoize是一样的

;使用delay和force实现三角数的惰性链表
(defn inf-triangles [n]
  {:head (triangle n)
   :tail (delay (inf-triangles (inc n)))})
(defn t-head [l] (:head l))
(defn tail [l] (force (:tail l)))
;这种实现和lazy-seq不同的地方是,节点的头总是会被计算的,即使它从来没有被访问
(def tri-nums (inf-triangles 1))
(t-head tri-nums)
;1
(t-head (tail tri-nums))
;3
(t-head (tail (tail tri-nums)))
;6
;使用一些工具函数来更好的获取这个链表中特定的元素
(defn taker [n l]
  (loop [t n,
         src l,
         ret []]
    (if (zero? t)
      ret
      (recur (dec t) (tail src) (conj ret (t-head src))))))
(taker 10 tri-nums)
;[1 3 6 10 15 21 28 36 45 55]
(defn nthr [l n]
  (if (zero? n)
    (t-head l)
    (recur (tail l) (dec n))))
(nthr tri-nums 99)
;5050
;可以看出,使用delay和force这样底层的宏去构建惰性序列是比较麻烦的
;最好还是最大限度的利用clojure自带的lazy seq

;使用lazy seq实现快速排序
(defn rand-ints [n]
  (take n (repeatedly #(rand-int n))))
(rand-ints 10)
;(0 2 1 5 5 8 2 4 6 3)
(defn sort-parts
  [work]
  (lazy-seq
    (loop [[part & parts] work]
      (if-let [[pivot & xs] (seq part)]
        (let [smaller? #(< % pivot)]
          (recur (list*
                   (filter smaller? xs)
                   pivot
                   (remove smaller? xs)
                   parts)))
        (when-let [[x & xss] parts]
          (cons x (sort-parts xss)))))))
(defn qsort
  [coll]
  (sort-parts (list coll)))
(qsort (rand-ints 20))
(qsort (rand-ints 5))
;这种实现的好处是不需要进行整体的排序,只在需要的时候计算一部分值
;这对于在很大的无序序列中取最小的n个值是很高效的
(take 10 (qsort (rand-ints 10000)))
;(0 0 2 5 6 8 9 9 9 9)
