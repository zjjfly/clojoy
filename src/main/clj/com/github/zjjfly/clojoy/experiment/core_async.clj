(ns com.github.zjjfly.clojoy.experiment.core-async
  (:require [clojure.core.async :as async]))

; 同步发送和接收消息
(let [c (async/chan 10)]
  (async/>!! c "hello")
  (assert (= "hello" (async/<!! c)))
  (async/close! c))

;使用thread方法,传入一个supplier,它会在另一个线程中执行,并将它的结果放到一个channel中,最后返回这个channel
(->
  (async/thread "hello")
  async/<!!
  (= "hello"))

;使用go,其中的阻塞的channel操作只会暂停执行,而不会阻塞线程
(let [c (async/chan)]
  (async/go (async/>! c "hello"))
  (assert (= "hello" (async/<!! (async/go (async/<! c)))))
  (async/close! c))

;alts!!类似go的select,可以同时等待多个channel的输出
(let [c1 (async/chan)
      c2 (async/chan)]
  (async/thread (while true
                  (let [[v ch] (async/alts!! [c1 c2])]
                    (println "Read" v "from" ch))))
  (async/>!! c1 "hi")
  (async/>!! c2 "there"))

;alts!是在go中使用的alts!!
(let [c1 (async/chan)
      c2 (async/chan)]
  (async/go (while true
              (let [[v ch] (async/alts! [c1 c2])]
                (println "Read" v "from" ch))))
  (async/go (async/>! c1 "hi"))
  (async/go (async/>! c2 "there")))

(let [n 1000
      cs (repeatedly n async/chan)
      begin (System/currentTimeMillis)]
  (doseq [c cs] (async/go (async/>! c "hi")))
  (dotimes [i n]
    (let [[v c] (async/alts!! cs)]
      (assert (= "hi" v))))
  (println "Read" n "msgs in" (- (System/currentTimeMillis) begin) "ms"))

(let [c (async/chan)]
  (async/go (do
              (Thread/sleep 3000)
                (async/>! c "hello")))
  (async/<!! (async/go
    (async/<! c)))
  (async/close! c))

;实现类似go的time.After
(defn time-after
  [n]
  (async/go (do
              (Thread/sleep n)
              nil)))
(async/<!! (time-after 3000))
