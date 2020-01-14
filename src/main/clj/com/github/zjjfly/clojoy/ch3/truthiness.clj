(ns com.github.zjjfly.clojoy.ch3.truthiness)

;对于clojure,它只对值的logic true or false感兴趣,所以使用的是truthiness这个词

;clojure中实际只有一个地方会去用到布尔型:if这个特殊形式,其他的and,or,when等都是基于if的宏

;clojure只认为false(Boolean.FALSE)和nil是logic false,其他都是true,这个js这样的语言差别很大
(if true :truthy :falsey)
;:truthy
(if [] :truthy :falsey)
;:truthy
(if nil :truthy :falsey)
;:falsey
(if false :truthy :falsey)
;:falsey

;不要使用Boolean构造函数去创建Boolean对象
(def evil-false (Boolean. "false"))                         ;NEVER do this
;它看上去和false没啥区别
evil-false
;false
(= false evil-false)
;true
(if evil-false :truthy :falsey)
;:truthy
;java的文档里警告了这种做法,这里再次警告
;如果实在需要手动构造Boolean对象,使用Boolean.parse
(if evil-false :truthy :falsey)

;如果你想要区分到底是nil还是false,可以使用nil?或false?
(nil? nil)
;true
(false? false)
;true

;空集合也是logic true的
(if [] 1 2)
;1
; 如果需要以空集合作为循环终止的条件,一种方法是使用empty?函数
(defn print-seq
  [s]
  (when-not (empty? s)
    (prn (first s))
    (recur (rest s))))

;但这样的写法不是很优雅,另一种方法是使用seq函数,如果它的参数是空集合,它返回的是nil
(defn print-seq
  [s]
  (when (seq s)
    (prn (first s))
    (recur (rest s))))
(print-seq [1 2])
;1
;2

;就这个例子而言,还有一种更简单的方法是使用doseq,clojure中以do开头的函数通常在body中有副作用,所以一般返回的是nil
(defn print-seq
  [s]
  (doseq [i s]
    (prn i)))
(print-seq [1 2])
;1
;2
