(ns clojoy.ch7.closure
  (:import (java.util.concurrent.atomic AtomicInteger)))

;闭包也是一种函数,但它可以访问其定义的时候的上下文(局部变量)
(def times-two
  (let [x 2]
    (fn [y] (* x y))))
(times-two 5)
;10
;上面的times-two是一个闭包,这个闭包可以访问函数体之外的局部变量x

;还可以使用闭包来隐藏可变内容
(def add-and-get
  (let [ai (AtomicInteger.)]
    (fn [y] (.addAndGet ai y))))
(add-and-get 2)
;2
(add-and-get 2)
;4
;可以看出调用add-and-get的时候访问的是同一个AtomicInteger
;但这样也有风险,它会让函数不再纯,函数的测试和推断会变得很困难

;使用函数生成闭包
(defn times-n
  [n]
  (fn [y] (* n y)))
(def times-four (times-n 4))
(times-four 1)
;4

(defn divisible
  [denom]
  (fn [num]
    (zero? (rem num denom))))
((divisible 3) 6)
;true
((divisible 3) 7)
;false

;将闭包作为函数传递
(filter (divisible 4) (range 10))
;(0 4 8)
;一种常见的做法是:在要用到局部变量的地方定义一个闭包,隐藏这些局部变量
(defn filter-divisible [denom s]
  (filter #(zero? (rem % denom)) s))
(filter-divisible 5 (range 20))
;(0 5 10 15)

;多个闭包共享同样的上下文
(def bearings [{:x 0 :y 1}                                  ;north
               {:x 1 :y 0}                                  ;east
               {:x 0 :y -1}                                 ;south
               {:x -1 :y 0}                                 ;west
               ])
(defn bot [x y bearing-num]
  {:coords [x y]
   :bearing ([:north :east :south :west] bearing-num)
   :forward (fn [] (bot (+ x (:x (bearings bearing-num)))
                        (+ y (:y (bearings bearing-num)))
                        bearing-num))
   :turn-right (fn [] (bot x y (mod (+ 1 bearing-num) 4)))
   :turn-left (fn [] (bot x y (mod (- 1 bearing-num) 4)))
   })
(:coords (bot 5 5 0))
;[5 5]
(:bearing (bot 5 5 0))
;:north
(:coords ((:forward (bot 5 5 0))))
;[5 6]
