(ns clojoy.ch15.memorize)

;clojure的memorize可以产生一个有记忆的函数,使用相同的参数调用这个函数,只会在第一次的时候真正执行,后续调用都是直接返回缓存中的结果
(def x (memoize (fn [x y]
                  (println "calculating...")
                  (+ x y))))
(x 1 2)
;calculating...
;=> 3
(x 1 2)
;3

;但它的问题是,对于所有的输入都存储结果,并且没有缓存更新和过期机制
;设计一个缓存的协议
(defprotocol Cache
  (lookup [cache e])  ;从缓存中查找某项
  (has? [cache e])    ;判断缓存中是否存在某项
  (hit [cache e])     ;当缓存命中时调用的函数
  (miss [cache e ret]);当缓存未命中时调用的函数
)

(deftype BasicCache [cache]
  Cache
  (lookup [_ item]
    (get cache item))
  (has? [_ item]
    (contains? cache item))
  (hit [this item] this)
  (hit [_ item ret]
    (BasicCache. (assoc cache item ret))))

;后续的代码可以参考clojure.core.cache
