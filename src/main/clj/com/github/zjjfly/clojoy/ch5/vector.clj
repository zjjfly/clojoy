(ns com.github.zjjfly.clojoy.ch5.vector)

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
(try
  (into (vector-of :int) [1 2 5412414145677946894501515])
  (catch Exception e
    (.getMessage e)))
;"Value out of range for long: 5412414145677946894501515"

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
(try
  (nth [1 2 3] 3)
  (catch Exception e
    (.getMessage e)))
;nil
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
;conj用于在vector右边添加一个元素
(conj my-stack 4)
;[1 2 3 4]
;list也实现了clojure.lang.IPersistentStack,但它认为的栈顶是list的最左边,这和vector是相反的
