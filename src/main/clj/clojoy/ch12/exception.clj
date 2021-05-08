(ns clojoy.ch12.exception
  (:require [clojoy.utils.assert :as a]))

;clojure中的异常一般分为两种:运行时异常和编译时异常

;运行时异常一般分为Exception和Error,clojure的惯用处理方法是:
;对于Exception,处理之后程序可以继续运行,而Error则表示程序应该终止

;对于编译时异常,一般出现在调用宏的时候
(defmacro do-something [x] `(~x))
(a/assert-error ClassCastException (do-something 1))
;orm throws a java.lang.ClassCastException(java.lang.Long cannot be cast to clojure.lang.IFn).
;更好的方式是在宏中就检测出异常并抛出,这样就能在编译的时候就发现,这也符合fail-fast原则
(defmacro pairs [& args]
  (if (even? (count args))
    `(partition 2 '~args)
    (throw (IllegalArgumentException. "pairs requires an even number of args"))))
;下面的代码会抛出异常
;(pairs 1 2 3)
;在定义函数的时候也会抛出异常
;(defn x [] (pairs 1 2 3))

;clojure处理错误的方式:
;1.让异常从内部的form流到最外部,然后处理
;2.在内部处理异常,通过动态var的特性,会在17章讨论
;下面是第一种方式的例子
(defmacro -?> [& forms]
  `(try (-> ~@forms)
        (catch NullPointerException _# nil)))
(-?> 25
     Math/sqrt
     (and nil)
     (+ 100))
;nil

;clojure提供了新的异常ExceptionInfo,并附带了ex-info和ex-data用于使用它
;ex-data可以用于任何错误类型
(defn perform-act
  [x y]
  (try
    (/ x y)
    (catch ArithmeticException ex
      (throw (ex-info "something went wrong!" {:args [x y]})))))
(try
  (perform-act 1 0)
  (catch RuntimeException e
    (println "Received error:" (.getMessage e))
    (when-let [ctx (ex-data e)]
      (println (str "More information: " ctx)))))
;Received error: something went wrong!
;More information: {:args [1 0]}
