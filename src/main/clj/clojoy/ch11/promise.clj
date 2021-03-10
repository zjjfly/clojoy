(ns clojoy.ch11.promise
  (:require [clojoy.utils.concurrency :as c]))

;promise类似future,不同的是,future可以封装任何的表达式在计算结束后的把值缓存起来
;而promise只是值的占位符,它的计算是在其他线程中使用deliver发送的
(def x (promise))
(def y (promise))
(def z (promise))
(c/dothreads! #(deliver z (+ @x @y)))

(c/dothreads!
 #(do (Thread/sleep 1000) (deliver x 52)))

(c/dothreads!
 #(do (Thread/sleep 2000) (deliver y 86)))

(time @z)
;"Elapsed time: 2003.11672 msecs"

;一个promise只能一次写入,重复deliver虽然不会抛出异常,但没有实际作用(1.3之前会抛出异常)
(deliver x 1)
(assert (= 52 @x))

;构建一个类似于as-futures的宏
(defmacro with-promises [[n tasks _ as] & body]
  (when as
    `(let [tasks# ~tasks
           n# (count tasks#)
           promises# (take n# (repeatedly promise))]
       (dotimes [i# n#]
         (c/dothreads!
          (fn []
            (deliver (nth promises# i#)
                     ((nth tasks# i#))))))
       (let [~n tasks#
             ~as promises#]
         ~@body))))

;使用这个宏实现一个简单的test runner
(defrecord Test [run passed failed])

(defn pass [] true)
(defn fail [] false)

(defn run-tests [& all-tests]
  (with-promises
    [tests all-tests :as results]
    (into (Test. 0 0 0)
          (reduce #(merge-with + %1 %2) {}
                  (for [r results]
                    (if @r
                      {:run 1 :passed 1}
                      {:run 1 :failed 1}))))))

(run-tests pass fail pass pass fail)
;#clojoy.ch11.promise.Test{:run 5, :passed 3, :failed 2}

;promise可以把基于回调的函数变成阻塞调用
(defn cps->fn [f k]
  (fn [& args]
    (let [p (promise)]
      (apply f (fn [x] (deliver p (k x))) args)
      @p)))
