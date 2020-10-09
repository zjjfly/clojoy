(ns clojoy.ch4.keyword)

;关键字以一个或多个冒号开头,它总是指向自身,而符号则可能指向任何合法的Clojure值或引用
;它的作用:
;1.作为map的key
;2.作为函数,用来查找map中对应的值
;3.作为枚举
;4.作为多重方法的分发值
;5.作为指令,通过指令来控制函数的行为,下面是一个例子,使用:toujours来控制函数的返回
(defn pour [lb ub]
  (cond
    (= ub :toujours) (iterate inc lb)
    :else (range lb ub)))
;这里的:else就是一种指令,实际上:else可以换成任何别的关键字,但不鼓励这么做

(pour 1 10)
;(1 2 3 4 5 6 7 8 9)

(class (pour 1 :toujours))
;clojure.lang.Iterate

;关键字不属于任何的命名空间
;即使那些以双冒号开头的关键字,实际也不和特定的命名空间绑定
::not-in-ns
;:clojoy.ch4.keyword/not-in-ns
;看上去确实有个命名空间前缀,但实际这只是clojure读取器加上去的
:haunted/in-another
;:haunted/in-another
;在当前命名空间中声明了一个看上去是属于haunted命名空间中关键字,但实际上根本没有这个命名空间
;但给关键字加上命名空间前缀是有意义的,可以让函数在不同的命名空间中调用的时候有不同的行为,下面是例子:
(defn do-blowfish [directive]
  (case directive
    :aquarium/blowfish (println "feed the fish")
    :crypto/blowfish (println "encode the message")
    :blowfish (println "not sure what to do")))
(ns crypto)
(require '(clojoy.ch4 [keyword :as keyword]))
(keyword/do-blowfish :blowfish)
;not sure what to do
(keyword/do-blowfish ::blowfish)
;encode the message
(ns aquarium)
(require '(clojoy.ch4 [keyword :as keyword]))
(keyword/do-blowfish ::blowfish)
;feed the fish

