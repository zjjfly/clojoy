(ns clojoy.ch12.array
  (:import (java.util Date)
           (java.sql Time))
  (:require [clojoy.utils.assert :as a]))

;clojure创建原生类型数组的几种方式
(int-array 3 0)
(make-array Integer/TYPE 3)
(into-array Integer/TYPE [1 2 3])

;创建引用类型的数组
(into-array ["a" "b" "c"])
(into-array [(Date.) (Time. 0)])
;into-array传入的集合的元素的类型如果不兼容,会报错
(a/assert-error IllegalArgumentException (into-array ["a" "b" 1M]))
;form throws a java.lang.IllegalArgumentException(array element type mismatch).

;要创建异构的数组,要使用to-array或to-array-2d
;注意,不管是to-array还是to-array-2d,都会对原生类型进行装箱
(def a (to-array-2d [[1]
                     [2 3]
                     [4 5 6]]))
(map alength [(aget a 0) (aget a 1) (aget a 2)])
;(1 2 3)

(to-array ["a" 1M #(%) (proxy [Object] [])])
; #object["[Ljava.lang.Object;" 0x33160e34 "[Ljava.lang.Object;@33160e34"]

;Java的数组是可变的,所以在clojure中使用要特别小心
;例如,seq作用于数组,返回的是一个视图,所以对原数组的修改会改变这个视图
(def ary (into-array [1 2 3]))
(def sary (seq ary))
sary
;(1 2 3)
(aset ary 0 0)
(assert (= 0 (first sary)))
;(0 2 3)

;repl打印数组,会有令人困惑的类名称如"[Ljava.lang.Object;",下面使用多重方法打印可读性更强的名称
(defmulti what-is class)
(defmethod what-is
  (Class/forName "[Ljava.lang.String;")
  [_]
  "1d string")
(defmethod what-is
  (Class/forName "[[Ljava.lang.Object;")
  [_]
  "2d object")
(defmethod what-is
  (Class/forName "[[[[I")
  [_]
  "Primitive 4d int")

(what-is (into-array ["a" "b"]))
;1d string
(what-is (to-array-2d [[1 2] [3 4]]))
;"2d object"
(what-is (make-array Integer/TYPE 2 2 2 2))
;"Primitive 4d int"

;想要从vector构建多维数组,不能简单的调用into-array
(a/assert-error IllegalArgumentException (what-is (into-array [[1.0] [2.0]])))
;form throws a java.lang.IllegalArgumentException(No method in multimethod 'what-is' for dispatch value: class [Lclojure.lang.PersistentVector;).
(defmethod what-is
  (Class/forName "[[D")
  [_]
  "Primitive 2d double")
(defmethod what-is
  (Class/forName "[Lclojure.lang.PersistentVector;")
  [_]
  "1d Persistent Vector")
(what-is (into-array [[1.0] [2.0]]))
"1d Persistent Vector"
(what-is (into-array (map double-array [[1.0] [2.0]])))
;"Primitive 2d double"

;调用Java的带可变参数的方法
(String/format "An int %d and a String %s"
               (to-array [99 "luftballons"]))
;"An int 99 and a String luftballons"
