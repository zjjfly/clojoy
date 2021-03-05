(ns clojoy.ch10.var
  (:require [clojoy.utils.assert :as a]))

;var是最常用的引用类型,因为两点:
;1.它在一个命名空间中可以命名和保留
;2.var可以提供线程的局部绑定(thread local)

;和其他引用对象不同的是,var不需要deref,直接对其名称求值就能得到值
*read-eval*
;true

;使用var可以得到var对象本身
(var *read-eval*)
;#'clojure.core/*read-eval*
;从上面的打印内容可以看出,var和#'表示的同样的事情
#'*read-eval*
;#'clojure.core/*read-eval*

;默认情况下,如果var在当前线程没有局部绑定,那么得到的是var的根绑定
;定义var的线程局部绑定的常用方式是binding
(defn print-read-val
  []
  (println "*read-val* is currently " *read-eval*))
(defn binding-play
  []
  (print-read-val)
  (binding [*read-eval* false]
    (print-read-val))
  (print-read-val))
(binding-play)

;创建var的方式是def以及展开式包含def的宏(如defn,defmacro,defonce,defmulti)
;def会在当前命名空间中寻找是否已存在同名的var,如果存在则使用这个已存在的var,否则创建一个新的var
;def返会的是这个var本身
(def favorite-color :green)
;#'clojoy.ch10.var/favorite-color
;#'可以阻止var被解引用
favorite-color
;:green

;var有四种状态

;1.未绑定值
(def ^:dynamic x)
(resolve 'x)
;#'clojoy.ch10.var/x
(bound? #'x)
;false
(thread-bound? #'x)
;false

;2.绑定值
(def ^:dynamic x 5)
(resolve 'x)
;#'clojoy.ch10.var/x
(bound? #'x)
;true
(thread-bound? #'x)
;false

;3.局部绑定
(binding [x 3]
  (resolve 'x))
;#'clojoy.ch10.var/x
(binding [x 3]
  (bound? #'x))
;true
(binding [x 3]
  (thread-bound? #'x))
;true

;4.匿名var,它不需要使用#'阻止解引用,它需要使用@或var-get解引用
(with-local-vars [x 9]
  (println @x))
;9
(with-local-vars [x 9]
  (bound? x))
;true
(with-local-vars [x 9]
  (thread-bound? x))
;true

;var和let定义的局部变量不同的地方是,它有动态作用域
;它的初始化可能在其他的命名空间中,还可以通过binding或展开式包含binding的宏声明一个局部的绑定
;with-precision就是这样的宏,它定义了BigDecimal的精度
(with-precision 4
  (/ 1 3M))
;0.3333M
;去掉with-precision会报错
(a/assert-error
 ArithmeticException
 (/ 1 3))

;有些函数会让with-precision报错
;下面的代码在repl中会报错,因为map是惰性的,在打印的时候才会实际执行,而这个时候已经出了with-precision的动态作用域
;(with-precision 4
;  (map (fn [x] (/ x 3)) (range 1M 4M)))
;可以使用doall规避这个问题
(with-precision 4
  (doall (map (fn [x] (/ x 3)) (range 1M 4M))))
;(0.3333M 0.6667M 1M)
;但更好的方法是使用bound-fn,它会在定义函数之前获取当前的线程的所有局部绑定,并在函数体中使用with-bindings*使用这些绑定
(with-precision 4
  (map (bound-fn [x] (/ x 3)) (range 1M 4M)))
; (0.3333M 0.6667M 1M)

