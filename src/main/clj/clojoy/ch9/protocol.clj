(ns clojoy.ch9.protocol
  (:import (clojoy.ch9.record TreeNode)
           (clojure.lang IPersistentVector))
  (:require [clojoy.ch9.record :as record]
            [clojoy.utils.assert :as assert]))

;协议实际上是一个方法签名的集合
;它里面的函数的第一个参数是调用函数的对象
;协议没有多重方法灵活,但可以有更好的性能以及相比Java更动态的行为

;定义一个适用于先进后出或先进先出的集合的协议
(defprotocol FIXO
  #_("添加fixo-作为前缀是避免和clojure内置函数发生冲突")
  (fixo-push [fixo value])
  (fixo-pop [fixo])
  (fixo-peek [fixo]))

;扩展协议的方式有extend,extend-type和extend-protocol
;对于record,使用extend-type是比较方便的
(extend-type TreeNode
  FIXO
  (fixo-push [node value]
    (record/xconj node value)))

(extend-type IPersistentVector
  FIXO
  (fixo-push [v value]
    (conj v value)))

;要完全实现一个协议,必须在一个extend form中实现协议的所有方法
;那如何在clojure中实现mixin呢?可以把实现拆分成多个单独的map,在需要实现某个协议的时候再组合成一个完整的协议实现
(defprotocol StringOps
  (rev [s])
  (upp [s]))
(def rev-mixin {:rev clojure.string/reverse})
(def upp-mixin {:upp clojure.string/upper-case})
(def full-mixin (merge rev-mixin upp-mixin))
(extend String
  StringOps full-mixin)
(rev "123")
;321
(upp "abc")
;"ABC"

;像刚刚那样为已有类型进行扩展,在Java中是不可能的,而clojure的协议甚至可以扩展到nil
;使用fixo-push构建一个clojoy.ch9.record中的sample-tree,会抛出异常,因为nil没有实现协议fixo-push
(assert/assert-error IllegalArgumentException (reduce fixo-push nil [3 5 2 4 6 0]))
;form throws a java.lang.IllegalArgumentException(No implementation of method: :fixo-push of protocol: #'clojoy.ch9.protocol/FIXO found for class: nil)
(extend-type nil
  FIXO
  (fixo-push [_ v]
    (TreeNode. v nil nil)))
(record/xseq (reduce fixo-push nil [3 5 2 4 6 0]))
;(0 2 3 4 5 6)

;为TreeNode提供完整的FIXO协议的实现
(extend-type TreeNode
  FIXO
  (fixo-push [node value]
    (record/xconj node value))
  (fixo-peek [node]
    (if (:l node)
      #_("这里使用的是recur去递归,而不是显示的调用fix-peek,这和后面的实现不一样.这种做法不支持多态,所以有可能会引起问题")
      (recur (:l node))
      (:val node)))
  (fixo-pop [node]
    (if (:l node)
      (TreeNode. (:val node) (fixo-pop (:l node)) (:r node))
      (:r node))))
(extend-type IPersistentVector
  FIXO
  (fixo-push [v value]
    (conj v value))
  (fixo-peek [v]
    (peek v))
  (fixo-pop [v]
    (peek v)))

(fixo-pop (reduce fixo-push nil [3 5 2 4 6 0]))

;如果有些方法和协议的具体实现类型无关,那么可以把这种方法定义为普通函数而不用放到协议里
(defn fixo-into
  [c1 c2]
  (reduce fixo-push c1 c2))
(record/xseq (fixo-into (TreeNode. 5 nil nil) [2 4 6 7]))
;(2 4 5 6 7)
(seq (fixo-into [1 2 3] [4 5 6]))
;(1 2 3 4 5 6)
;使用extend也可以达到类似的目的,就像之前说的是mixin
(def tree-node-fixo
  {:fixo-push (fn [node value]
                (record/xconj node value))
   :fixo-peek (fn [node]
                (if (:l node)
                  (recur (:l node))
                  (:val node)))
   :fixo-pop  (fn [node]
                (if (:l node)
                  (TreeNode. (:val node) (fixo-pop (:l node)) (:r node))
                  (:r node)))})
(extend TreeNode FIXO tree-node-fixo)
(record/xseq (fixo-into (TreeNode. 5 nil nil) [2 4 6 7]))
;(2 4 5 6 7)

;大多数情况下,这种方式比第七章中使用过的闭包函数好
;使用reify可以把闭包的优势和性能以及对协议的扩展全部放在一个form里
(defn fixed-fixo
  ([limit] (fixed-fixo limit []))
  ([limit vector]
   (reify FIXO
     (fixo-push [this value]
       (if (< (count vector) limit)
         (fixed-fixo limit (conj vector value))
         this))
     (fixo-peek [_] (peek vector))
     (fixo-pop [_]
       (pop vector)))))

;协议相比于Java的接口的优势是,它的方法是和命名空间绑定的,所以一个记录可以扩展不同协议的同名方法

;defrecord可以直接扩展协议或实现接口
(defrecord TNode [val l r]
  FIXO
  (fixo-push [_ v]
    (if (< v val)
      (TNode. val (fixo-push l v) r)
      (TNode. val l (fixo-push r v))))
  (fixo-peek [_]
    (if l
      #_("如果这里使用recur,则直接recur不需要加参数,这和上面的extend中的不一样")
      (fixo-peek l)
      val))
  (fixo-pop [_]
    (if l
      (TNode. val (fixo-pop l) r)
      val)))
(def sample-tree2 (reduce fixo-push (TNode. 3 nil nil) [5 2 4 6]))
(record/xseq sample-tree2)
;(2 3 4 5 6)
;这样实现的好处是:
; 1.在记录类型上调用协议中的方法,相比使用extend form实现的对象,在性能上好几倍
; 2.可以直接使用记录的字段,它们在这种情况是局部变量,所以代码更简洁

;defrecord还可以实现Java的接口和扩展Object
(defrecord Task [f args]
  Runnable
  (run [_]
    (println (apply f args))))
(.start (Thread. (Task. + [1 2 3])))
