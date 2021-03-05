(ns clojoy.ch9.move)

;相对于Java的FluentMove,clojure的实现更加简洁和灵活
(defn build-move
  [& pieces]
  (apply hash-map pieces))

(build-move :from "e7" :to "e8" :promotion \Q)
;{:from "e7", :promotion \Q, :to "e8"}

;但这种实现的缺点是它没有提供和Java实现一样的输出形式
;一种解决方案是使用记录类型
(defrecord Move [from to castle? promotion]
  Object
  (toString [this]
    (str "Move " (:from this)
         " to " (:to this)
         (if (:castle? this)
           " castle"
           (if-let [p (:promotion this)]
             (str " promotion to " p)
             "")))))
(str (Move. "e2" "e4" nil nil))
;"Move e2 to e4"
(.println System/out (Move. "e7" "e8" nil \Q))
;Move e7 to e8 promotion to Q

;但使用记录类型又有了需要按照位置构造的问题,虽然可以通过记录类型自带的map->Move函数一定程度上规避
;还有一个方法是把clojure的map和Java类结合起来使用
(defn build-move [& {:keys [from to castle? promotion]}]
  {:pre [from to]} #_{"使用前置条件约束输入"}
  (Move. from to castle? promotion))
(str (build-move :from "e2" :to "e4"))
;"Move e2 to e4"
