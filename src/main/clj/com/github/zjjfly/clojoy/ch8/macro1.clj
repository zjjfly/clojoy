(ns com.github.zjjfly.clojoy.ch8.macro1
  (:require [clojure.walk :as walk]))

;宏的一个典型应用场景是把一个form转换成另一个form
;宏->可以把多个form串起来
(-> (/ 144 12) (/, 2 3) str keyword list)                   ;,在这里作为辅助标记符,不是常见用法
;(:2)

;eval,传入一个clojure表达式的数据结构,对其求值
(eval 1)
;1
(eval '(list 1 2))
;(1 2)
(eval (list (symbol "+") 1 2))
;eval的问题是它解析的时候使用的bindings是全局的
;一个限制binding范围的方法
(defn context-eval [ctx expr]
  (eval
    `(let [~@(mapcat (fn [[k v]] [k `'~v]) ctx)]
       ~expr)))
(context-eval '{a 1, b 2} '(+ a b))
;3
(context-eval '{a 1, b 2} '(let [b 1000] (+ a b)))
;1001

;语法quote和反quote是可以相互抵消的
(let [x 9, y '(- x)]
  (println `y)
  (println ``y)
  (println ``~y)
  (println ``~~y)
  (context-eval {'x 36} ``~~y))

`(~@(mapcat (fn [[k v]] [k `'~v]) {:a 1}))

;一些关于宏的经验之谈:
;1.能使用函数就不使用宏
;2.编程测试
;3.使用macroexpand或者clojure.walk的macroexpand-all等展开宏,查看展开的form是否符合预期
;4.在REPL中实验
;5.尽可能将复杂的宏分解成小函数

;clojure中的大多控制结构都是使用宏定义的,所以我们来定义一些控制结构作为学习宏的起始
;宏可以使用语法quote,也可以不用.先不使用语法quote
;defmacro用于定义宏,传给它的实参不会先求值,而是保留原来的结构
;自己实现的版本
(defmacro do-until [& form]
  (let [pred->do (partition 2 form)
        need-do (take-while
                  #(eval (first %))
                  pred->do)]
    (->> (for [[_ todo] need-do]
           todo)
         (cons 'do))))
(do-until (> 2 1) (println "a")
          (even? 2) (println "b"))
;a
;b
;这种实现的问题是使用了eval,而eval是无法解析局部变量的
;书上的实现
(defmacro do-until
  [& clauses]
  (when clauses
    (list 'clojure.core/when (first clauses)
          (if (next clauses)
            (second clauses)
            (throw (IllegalArgumentException.
                     "do until requires an even number of  forms")))
          (cons 'do-until (nnext clauses)))))
(let [x 1]
  (do-until true (prn 1)
            (> x 0) (prn 2)))
;1
;2

(macroexpand-1 '(do-until
                  true (prn 1)
                  false (prn 2)))

(walk/macroexpand-all '(do-until
                         true (prn 1)
                         false (prn 2)))
;(if true (do (prn 1) (if false (do (prn 2) nil))))

;使用语法quote和反quote定义宏,实现一个类似自己的when-no
(defmacro unless
  [condition & body]
  `(if (not ~condition)
     (do ~@body)))
;注意,第三行的~是必要的,否则执行的时候会去尝试解析命名空间中condition这个var,它很可能不存在,即使存在,也可能在逻辑上是true的
(unless true (println "nope"))
;nil
(unless false (println "yep!"))
;yep!
;=>nil

;用宏组合多种form
;以实现一个可以定义var的同时加上watch函数的宏
(defmacro def-watched
  [name & value]
  `(do (def ~name ~@value)
       (add-watch (var ~name)
                  :re-bind
                  (fn [~'key ~'r old# new#]
                    (println old# "->" new#)))))
(def-watched x 10)
(def x 11)
;10->11
