(ns com.github.zjjfly.clojoy.ch7.recursive
  (:require
    [com.github.zjjfly.clojoy.utils.assert :as assert]))

;普通递归,显示的调用自身
(defn pow
  [base exp]
  (if (zero? exp)
    1
    (* base (pow base (dec exp)))))
(pow 10 2)
;100
;普通递归的问题是如果递归次数太多,会抛出StackOverFlow
(assert/assert-error StackOverflowError (pow 1 100000))
;form throws a java.lang.StackOverflowError()

;防止堆栈溢出的方法是把普通递归变成尾递归
(defn pow
  [base exp]
  (letfn [(kapow [base exp acc]
            (if (zero? exp)
              acc
              (recur base (dec exp) (* base acc))))]
    (kapow base exp 1)))
(pow 1 10000)
;1

;还有一种解决方法是在普通递归中使用lazy seq
(def simple-metric {:meter 1
                    :km    1000
                    :cm    1/100
                    :mm    [1/10 :cm]})
(defn convert [context descriptor]
  (reduce (fn [result [mag unit]]
            (+ result
               (let [x (get context unit)]
                 (if (vector? x)
                   (* mag (convert context x))
                   (* mag x)))))
          0
          (partition 2 descriptor)                          ;partition的结果就是一个lazy seq
          ))
(convert simple-metric [100 :meter])
;100
(convert simple-metric [50 :cm])
;1/2
(convert simple-metric [100 :mm])
;1/10
(float (convert simple-metric [3 :km 10 :meter 80 :cm 10 :mm]))
;3010.81
;convert不受单位个数的约束,而且不限于长度
(convert {:bit 1 :byte 8 :nibble [1/2 :byte]} [32 :nibble])
;128N

;clojure没有实现广义的尾递归调用优化,只能对尾递归调用自身的情景进行优化,这是因为JVM和其字节码没有支持
;而且clojure需要显式的实验recur来告诉编译器要做尾递归优化,这和scala是不同的

;clojure也可以优化互相递归调用的函数关系,通过trampoline函数
;模拟一个电梯,它有四种状态:一楼开,一楼关,二楼开和二楼关,起始于调用ff-open
;如果不使用trampoline,
(defn elevator [commands]
  (letfn
    [(ff-open [[_ & r]]
       (case _
         :close (ff-closed r)
         :done true
         false))
     (ff-closed [[_ & r]]
       (case _
         :open (ff-open r)
         :up (sf-closed r)
         false))
     (sf-closed [[_ & r]]
       (case _
         :down (ff-closed r)
         :open (sf-open r)
         false))
     (sf-open [[_ & r]]
       (case _
         :done true
         :close (sf-closed r)
         false))]
    (ff-open commands)))
(elevator [:close :open :close :up :open :open :done])
;false
(elevator [:close :up :open :close :down :open :done])
;true
(assert/assert-error StackOverflowError
                     (elevator (take 100000 (cycle [:close :open]))))
;form throws a java.lang.StackOverflowError()
;使用trampoline
(defn elevator [commands]
  (letfn
    [(ff-open [[_ & r]]
       #(case _
          :close (ff-closed r)
          :done true
          false))
     (ff-closed [[_ & r]]
       #(case _
          :open (ff-open r)
          :up (sf-closed r)
          false))
     (sf-closed [[_ & r]]
       #(case _
          :down (ff-closed r)
          :open (sf-open r)
          false))
     (sf-open [[_ & r]]
       #(case _
          :done true
          :close (sf-closed r)
          false))]
    (trampoline ff-open commands)))
(elevator (take 100000 (cycle [:close :open])))
;false
;trampoline的用法:
;1.让相互调用的函数返回一个函数,而不是通常的结果,一般只要在函数体外套一个#就可以
;2.通过trampoline函数调用函数链上的第一个函数
;相互调用函数的典型例子是状态机

;延续传递风格(continuation-passing style),是一种常用的写递归的风格,但在clojure中很少使用
;它有三部分组成:
;1.接收函数:确定递归什么时候终止
;2.返回延续:用以封装返回值
;3.延续函数:用以提供计算的下一步
;以阶乘为例子
(defn fac-cps
  [n k]
  (letfn [(cont [v] (k (* v n)))]
    (if (zero? n)
      (k 1)
      (recur (dec n) cont))))
(defn fac [n]
  (fac-cps n identity))
(fac 10)
;3628800
;这种模式的好处是,可以抽象出一个通用的函数构建器
(defn mk-cps [accept? kend kont]
  (fn [n]
    ((fn [n k]
       (let [cont (fn [v]
                    (k (kont v n)))]
         (if (accept? n)
           (k 1)
           (recur (dec n) cont))))
     n kend)))
(def fac
  (mk-cps zero? identity #(* %1 %2)))
(fac 10)
;3628800


;使用宏实现CPS
(defmacro cps [accept? kend kont]
  `(fn [n# acc#]
     (if (~accept? n#)
       (~kend acc#)
       (recur (dec n#) (~kont acc# n#)))))
((cps zero? identity #(* %1 %2)) 10 1)
;3628800
((cps zero? identity #(+ %1 %2)) 10 0)

;clojure中很少用CPS的原因是:
;1.没有广义尾递归调用优化,延续调用的数量受限于堆栈的大小
;2.在异常处理的情况下,CPS可能会让失败点涌出
;3.CPS无助于并行
