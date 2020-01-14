(ns com.github.zjjfly.clojoy.ch2.loop)

;clojure的循环使用的是loop/recur form
;recur专用于尾递归,它总会循环会最近的外部fn或loop,fn的参数或loop的局部变量会重新绑定为recur后面的表达式的值
(defn print-down-from [x]
  (when (pos? x)
    (println x)
    (recur (dec x))))

(print-down-from 3)
;3
;2
;1

;上面的例子只关心副作用,下面是一个累加器的例子
(defn sum-down-from [sum x]
  (if (pos? x)
    (recur (+ sum x) (dec x))
    sum))
(sum-down-from 0 10)
;55

;上面的两个例子一个使用了when,一个使用if
;一般使用when的情况是:
; 1.不需要else部分同条件式的结果相关联
; 2.需要隐式的do,依次执行有副作用的操作

;如果不想要循环会函数顶部,可以使用loop
;使用loop重写sum-down-from是的sum称为函数的局部变量
(defn sum-down-from' [^long initial-x]
  (loop [sum 0 x initial-x]
    (if (pos? x)
      (recur (+ sum x) (dec x))
      sum)))
(sum-down-from' 10)
;55

;注意,recur只能出现在函数的结尾的位置,处于非结尾的位置编译出错
;(fn [x]
;  (recur x)
;  (println x))
;Syntax error (UnsupportedOperationException) compiling recur at (loop.clj:40:3).
;Can only recur from tail position
