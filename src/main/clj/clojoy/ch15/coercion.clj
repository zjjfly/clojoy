(ns clojoy.ch15.coercion)

;coercion(强制类型转换),这种优化方式是让clojure尽量使用Java的原生类型
;实现一个阶乘函数
(defn factorial-a
  [original-x]
  (loop [x original-x
         acc 1]
    (if (>= 1 x)
      acc
      (recur (dec x) (* x acc)))))

(factorial-a 10)
;3628800
(factorial-a 20)
;2432902008176640000

;测试性能
(time (dotimes [_ 1e5] (factorial-a 20)))
;"Elapsed time: 38.160898 msecs"

(defn factorial-b
  [original-x]
  (loop [x (long original-x)
         acc 1]
    (if (>= 1 x)
      acc
      (recur (dec x) (* x acc)))))
(time (dotimes [_ 1e5] (factorial-b 20)))
;"Elapsed time: 20.090052 msecs"
;性能提升不明显?

(defn factorial-c
  [^long original-x]
  (loop [x original-x
         acc 1]
    (if (>= 1 x)
      acc
      (recur (dec x) (* x acc)))))
(time (dotimes [_ 1e5] (factorial-c 20)))
;"Elapsed time: 20.438069 msecs"

;clojure的数值计算会检查是否溢出,这也会消耗性能,可以关闭它
(set! *unchecked-math* true)
(defn factorial-d
  [^long original-x]
  (loop [x original-x
         acc 1]
    (if (>= 1 x)
      acc
      (recur (dec x) (* x acc)))))
(set! *unchecked-math* false)
(time (dotimes [_ 1e5] (factorial-d 20)))
;"Elapsed time: 6.046152 msecs"
;对性能的提升比较明显

;如何即避免溢出又能得到正确答案,有两种方式:
;1.使用double
(defn factorial-e
  [^double original-x]
  (loop [x original-x
         acc 1.0]
    (if (>= 1.0 x)
      acc
      (recur (dec x) (* x acc)))))
(time (dotimes [_ 1e5] (factorial-e 20)))
;"Elapsed time: 8.182712 msecs"
(factorial-e 20)
;用double的精度是一个问题
;2.43290200817664E18
(factorial-e 30)
;2.652528598121911E32
;double也会溢出,只是没那么容易
(factorial-e 171.0)
;##Inf

;如果需要保持精度,并且可以牺牲部分性能,那么可以使用clojure的会自动转型的数值操作函数(+',*',-',inc',dec')
(defn factorial-f
  [^long original-x]
  (loop [x original-x
         acc 1]
    (if (>= 1 x)
      acc
      (recur (dec x) (*' x acc)))))
(time (dotimes [_ 1e5] (factorial-f 20)))
;"Elapsed time: 21.711123 msecs"
;自动会转成BigInt,不会影响精度
(factorial-f 30)
;265252859812191058636308480000000N
;这种方式更不容易溢出
(factorial-f 171)
;1241018070217667823424840524103103992616605577501693185388951803611996075221691752992751978120487585576464959501670387052809889858690710767331242032218484364310473577889968548278290754541561964852153468318044293239598173696899657235903947616152278558180061176365108428800000000000000000000000000000000000000000N
