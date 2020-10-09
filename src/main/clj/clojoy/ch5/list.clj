(ns clojoy.ch5.list)

;list是最简单的clojure持久化集合,它是一个单链表,它的最左边是集合尾部,遍历是从左到右遍历
(doseq [i '(1 2 3)]
  (println i))
;1
;2
;3
;只能在左端向list添加元素
(conj '(1 2 3) 4)
;(4 1 2 3)

;list主要用来表示一个代码形式,用来调用函数,宏.代码形式也会被以编程的方式构建,求值或作为宏的返回
;如果不是作为clojure代码,list相比vector没有任何优势.

;clojure的list和传统lisp的list很相似,有一些函数名称的上的差别
;一个最主要的差别之一是,cons的行为的不同.
;在传统lisp中,使用cons为list添加元素,而在clojure中则使用conj
;而且,使用conj构建的list,在next链上的对象都保证是list
(class (next (conj '(1 2 3) 4)))
;clojure.lang.PersistentList
;使用cons构建的序列,next链上的对象是某种类型的seq
(class (next (cons 4 [1 2 3])))
;clojure.lang.PersistentVector$ChunkedSeq
;所以cons一般用于向(除了list的)其他类型的seq的头部添加元素,而conj用于在list头部添加元素

;还有一个区别是clojure没有dotted pair,如果要表示一个对结构,使用vector

;clojure中的seq的打印结果都是圆括号包括的,所以无法通过打印结果来判断集合类型,它们的行为有可能也是不同的
;比如,对list使用count函数,复杂度是O(1),而对于有些seq,它的复杂度是O(n).
;clojure的list和其他lisp的list不同的地方是:它是不可变的

;list可以作为stack,这和vector类似.具体使用哪个可以根据性能测试的结果决定

;list不适合用做随机访问的集合,因为通过索引去查找项必须从头遍历list
;它也不是set,原因同vector,而且list的contains?总是返回false
;它也不适合做为队列,因为list可以在一端添加元素,但无法在另一端删除项
