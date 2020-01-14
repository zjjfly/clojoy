(ns com.github.zjjfly.clojoy.ch1.encapsulation)

;clojure的封装代码的方式有两种
;一种是常见的基于namespace的封装,不赘述
;另一种是block级封装,让一些内部的帮助函数值限定在一个代码块中.一般使用letfn
;看一个例子
(letfn [(index [file rank]
          (let [f (- (int file) (int \a))
                r (* 8 (- 8 (- (int rank) (int \0))))]
            (+ f r)))]
  (defn lookup [board pos]
    (let [[file rank] pos]
      (board (index file rank)))))

;这样的做法可以控制哪些API暴露给客户

;还可以进一步收缩作用域,让所有的实现都在函数内部
(defn lookup2
  [board pos]
  (let [[file rank] (map int pos)
        [fc rc] (map int [\a \0])
        f (- file fc)
        r (* 8 (- 8 (- rank rc)))
        index (+ f r)]
    (board index)))
