(ns com.github.zjjfly.clojoy.ch5.vector
  (:require
    [com.github.zjjfly.clojoy.utils.assert :as assert]))

;vector是clojure中最常用的集合,无论数据量多还是少,它的性能都很好
;它更常用的原因大概是clojure中的括号已经够多了吧😹.
;用vector表示let,with-open,fn等的绑定是一种惯用法,我们自己写宏的时候也应该遵循

;vector字面量
[1 2 3]

;把集合转换成vector
(vec (range 10))
;[0 1 2 3 4 5 6 7 8 9]

;把一个序列的元素放入已有的vector中
(into [1 2 3] (range 4 10))
;[1 2 3 4 5 6 7 8 9]
;into的复杂度接近O(n)

;从多个单独的对象构建vector
(vector 1 2 3)
;[1 2 3]

;使用vector-of可以让vector存储原始类型
;它的参数是一个表示特定原始类型的keyword,可以使:int,:long,:float,:double,:byte,:short.:boolean或:char
;它返回的是一个空的vector,它的行为和其他的vector是一样的,只是内部存储的是原始类型
(def primary-vec (vector-of :int))
(into primary-vec [Math/PI 2 1.3])
;[3 2 1]
;这种vector在放入新元素的时候,放入的元素会被强制转换成初始化的时候指定的原始类型
(into (vector-of :char) [100 101 102])
;[\d \e \f]
(assert/assert-error IllegalArgumentException
                     (into (vector-of :int) [1 2 5412414145677946894501515]))
;orm throws a java.lang.IllegalArgumentException(Value out of range for long: 5412414145677946894501515)

;大vector对于在集合右端添加删除,通过数字索引访问或修改元素以及反向遍历依然是很高效的
(def a-to-j (vec (map char (range 65 75))))
;通过索引获取元素
(nth a-to-j 4)
;\E
(a-to-j 4)
;\E
(get a-to-j 4)
;\E
;这三种首推nth,它不会当vector是nil的时候抛出异常,会在越界的时候抛出异常,并支持未找的默认值
(nth nil 1)
;nil
(assert/assert-error IndexOutOfBoundsException (nth [1 2 3] 3))
;form throws a java.lang.IndexOutOfBoundsException()
(nth [] 1 :woops)
;:woops

;由于可以通过索引访问元素,所以从任何方向都可以高效的遍历的vector,seq和rseq就是这么做的
(seq a-to-j)
;(\A \B \C \D \E \F \G \H \I \J)
(rseq a-to-j)
;(\J \I \H \G \F \E \D \C \B \A)

;使用assoc修改vector元素
(assoc a-to-j 4 "no longer E")
;[\A \B \C \D "no longer E" \F \G \H \I \J]
;assoc还可以实现在尾部加元素,但这种做法不推荐,更好的是使用conj
;assoc在很多函数内部被使用,如replace,这个函数可以用于vector和seq,如果是vector,那么它会使用assoc处理
(replace {2 :a 4 :b} [1 2 3 2 3 4])
;[1 :a 3 :a 3 :b]
;assoc-in,get-in和update-in可以处理vector或map嵌套的结构
(def matrix
  [[1 2 3]
   [4 5 6]
   [7 8 9]])
(get-in matrix [1 2])
;6
(assoc-in matrix [1 2] 'x)
;[[1 2 3] [4 5 x] [7 8 9]]
(update-in matrix [1 2] * 100)
;[[1 2 3] [4 5 600] [7 8 9]]

;找二位矩阵中的一个点的邻居点
(defn neighbors
  ([size xy]
   (neighbors [[-1 0] [0 -1] [1 0] [0 1]]
              size xy))
  ([delta size xy]
   (let [check-fn (fn [xy]
                    (every? #(< -1 % size) xy))]
     (when (check-fn xy)
       (->> delta
            (map #(map + % xy))
            (filter check-fn))))))
(neighbors 3 [0 0])
;((1 0) (0 1))
(neighbors 3 [1 1])
;((0 1) (1 0) (2 1) (1 2))
(map #(get-in matrix %) (neighbors 3 [0 0]))
;(4 2)

;vector用作栈,因为它实现了clojure.lang.IPersistentStack这个接口
(def my-stack [1 2 3])
;pop返回的是去掉了最右边的元素之后的一个新的vector,这和其他语言中的pop的行为不一样
(pop my-stack)
;[1 2]
;使用peek获取栈顶元素,last也可以做到同样的是,但vector作为栈,在语义上还是使用peek更好,而且peek效率更高
(peek my-stack)
;3
;conj用于在栈中添加元素,这个函数可以用于其他持久化集合类型
(conj my-stack 4)
;[1 2 3 4]
;list也实现了clojure.lang.IPersistentStack,但它认为的栈顶是list的最左边,这和vector是相反的

;vector可以高效的在右边添加元素,并从左到右遍历,这个特性让clojure中很少使用reverse这个函数,这不同于传统的lisp
;传统的lisp的做法:
(defn strict-map1 [f coll]
  (loop [coll coll
         acc nil]
    (if (empty? coll)
      (reverse acc)
      (recur (next coll)
             (cons (f (first coll)) acc)))))
(strict-map1 - (range 5))
;(0 -1 -2 -3 -4)
;clojure的做法
(defn strict-map2 [f coll]
  (loop [coll coll
         acc []]
    (if (empty? coll)
      acc
      (recur (next coll)
             (conj acc (f (first coll)))))))
(strict-map2 - (range 5))
;[0 -1 -2 -3 -4]

;subvec可以从已有的vector中生成一个子vector
;它有两个参数,第一个是起始的index,它包含在子vector中,第二个是结束的index,它不包含在子vector
(subvec [1 2 3 4 5] 1 4)
;[2 3 4]
;子vector持有的引用是原有的vector的,子vector的子vector也是如此

;clojure中,遍历map的迭代器是seq,它的每一项的类型是MapEntity,它实际上是vector
(first {:width 10 :height 20 :depth 15})
;[:width 10]
(vector? (first {:width 10 :height 20 :depth 15}))
;true
;所以MapEntity可以使用vector支持的所有函数,包括conj,get,甚至解构
(doseq [[dimension amount] {:width 10 :height 20 :depth 15}]
  (println (str (name dimension) ":") amount "inches"))
;width: 10 inches
;height: 20 inches
;depth: 15 inches

;MapEntity有key和val两个函数,获取键和值.但最常用的还是解构
(key (first {:width 10 :height 20 :depth 15}))
;:width
(val (first {:width 10 :height 20 :depth 15}))
;10

;vector不适用的场景:
;1.不适合作为稀疏矩阵,因为它无法略过一些索引,在更高的索引中插入值
;2.它不可以在已有的值当中插入或删除值(可以删除最后的一个)
;3.它不适合作为队列.因为如果使用rest或next得到弹出一个值之后的集合,那么这个集合的类型不是vector
;需要用into或vec转回vector.如果使用subvec,那么得到的的vector底层还是使用的原来的vector,弹出的值不会被垃圾回收
;4.它无法使用contains函数来判断是否含有某个值,因为它查找的键,而不是值
(contains? [1 2 3 4] 0)
;true
