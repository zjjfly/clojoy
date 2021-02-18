(ns clojoy.ch9.type
  (:import (clojure.lang ISeq IPersistentStack Seqable))
  (:require [clojoy.ch9.protocol :as p]))

;有些时候,使用defrecord无法完成一些事情,例如用于实现clojure的ISeq接口
;(defrecord InfiniteConstant
;  [i]
;  ISeq
;  (seq [this]
;    (lazy-seq (cons i (seq this)))))
;Syntax error (ClassFormatError) compiling deftype* at (type.clj:5:1).
;Duplicate method name "seq" with signature "()Lclojure.lang.ISeq;" in class file
;原因是defrecord默认实现了clojure的map,其中已有seq方法,所以会出现函数重复的错误
;这种情况可以使用deftype,它没有默认实现任何接口或抽象类

(deftype InfiniteConstant
  [i]
  ISeq
  (seq [this]
    (lazy-seq (cons i (seq this)))))
(take 3 (InfiniteConstant. 5))
;(5 5 5)
;deftype中声明的类的字段是public的,可以直接访问
(.i (InfiniteConstant. 5))
;5

(deftype TreeNode [val l r]
  p/FIXO
  (p/fixo-push [_ v]
    (if (< v val)
      (TreeNode. val (p/fixo-push l v) r)
      (TreeNode. val l (p/fixo-push r v))))
  (p/fixo-peek [_]
    (if l
      (p/fixo-peek l)
      val))
  (p/fixo-pop [_]
    (if l
      (TreeNode. val (p/fixo-pop l) r)
      val))
  IPersistentStack
  (cons [this v] (p/fixo-push this v))
  (pop [this] (p/fixo-pop this))
  (peek [this] (p/fixo-peek this))
  Seqable
  (seq [t]
    (concat (seq l) [val] (seq r))))

(extend-type nil
  p/FIXO
  (p/fixo-push [_ v]
    (TreeNode. v nil nil)))

(def sample-tree (into (TreeNode. 3 nil nil) [5 2 4 6]))
(seq sample-tree)

;deftype是clojure提供的一种让我们可以定义volatile和可变字段的机制
