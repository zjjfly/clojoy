(ns com.github.zjjfly.clojoy.ch5.common_concept
  (:import (java.util ArrayList)))

;先区分clojure中sequential,sequence,seq这几个概念
;sequential(顺序的)集合指的是持有一系列值而不会对它们重新排序的集合
;sequence是一种顺序集合,它表示一系列可能存在也可能不存在的值,它们可能来自具体的集合,也可能是计算出来的.可以为空
;seq是序列抽象,它最主要的两个方法是:first和rest,通过这两个函数可以实现对序列的遍历
;还有一个叫seq的函数,它接受各种集合作为参数,返回一个实现了seq抽象的对象(如果原集合已经实现了seq,直接返回这个集合本身)

;seq函数和seq抽象定义的函数可以操作集合,字符串,数组,就好像它们都是序列一样
(first "1")
;\1
(first [1 2 3])
;1
(rest (into-array [1 2 3]))
;(2 3)

;clojure把集合分为三类:序列,map和set
;如果两个对象属于不同的类型,它们绝不会相对

;如果两个序列的元素相对且顺序相同,那么=就会返回true,即使它们的具体类型不同
(= [1 2 3] '(1 2 3))
;true
;相反,如果两个集合有相同的元素且顺序相对,但一个是序列,一个不是,那么=还是返回false
(= [1 2 3] #{1 2 3})
;false

;序列集合包括Clojure的list和vector,还有所有实现了Java的java.util.List接口的集合
;其他两个类型很容易分辨,一般名字中都带set或map
(= (ArrayList. [1 2 3]) [1 2 3])
;true

;每种clojure集合至少都提供了一种seq对象用来遍历其中的内容,可以通过seq函数暴露出来
;有的还不止一种,如vector的rseq,map的keys和vals,它们都返回一个序列,如果集合为空返回的是nil
(class (seq (hash-map :a 1)))
;clojure.lang.PersistentHashMap$NodeSeq
;可以看出它的类型是一个PersistentHashMap的一个内部类,它实现了序列接口
(class (keys (hash-map :a 1)))
;clojure.lang.APersistentMap$KeySeq
;keys返回的是另一个类型的序列
