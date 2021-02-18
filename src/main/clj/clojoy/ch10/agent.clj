(ns clojoy.ch10.agent
  (:require [clojoy.utils.concurrency :as c]))

;agent表示一个值会随着时间变化的标识.每个agent都有队列,存放对agent值的操作
;每个操作产生一个新的值,并作为下一个操作的输入

;发送操作到队列的方法是send和send-off,在事务中,它们可以被认为是没有副作用的,可以重试
(def joy (agent []))
(send joy conj "First edition")
@joy
;["First edition"]

;如果是一个耗时操作,则在操作发送之后有可能看到的还是旧的值
(defn slow-conj [coll item]
  (Thread/sleep 3000)
  (conj coll item))
(send joy slow-conj "Second edition")
;使用await可以让当前线程阻塞知道agent执行完所有在队列中的操作
(await joy)

;agent的常用场景是对资源进行序列化访问,如文件或其他IO流
;例子:多个线程汇报不同任务的进度,给每个报告一个全局的自增的id
(def log-agent (agent 0))

(defn do-log [msg-id message]
  (println msg-id ":" message)
  (inc msg-id))

(defn do-step [channel message]
  (Thread/sleep 10) #_("模拟工作")
  (send-off log-agent do-log
            (str channel message)))

(defn three-step [channel]
  (do-step channel " ready to begin (step 0)")
  (do-step channel " warming up (step 1)")
  (do-step channel " really getting going now (step 2)")
  (do-step channel " done! (step 3)"))

(defn all-together-now []
  (c/dothreads! #(three-step "alpha"))
  (c/dothreads! #(three-step "beta"))
  (c/dothreads! #(three-step "omega")))

(all-together-now)

;await和await-for可以发送线程阻塞,直到发送给这些agent的所有动作都完成
;如何要确保特定的消息在处理之前都已经写出去,可以使用它们
(do-step "importance: " "this must go out")
(await log-agent)
;await-for和await的区别是,它可以指定超时时间,返回false如果超时,其他情况返回true
(await-for 100 log-agent)
;true

;agent的操作集合是开放的,所以可以做一些agent设计之初未曾构想的事情
;例如跳到特定的msg-id
(send log-agent (fn [_] 1000))
(do-step "epsilon " "near miss")
;1000 : epsilon near miss

;send和send-off的区别:send会使用线程池,不同的agent会使用线程池中的线程去执行操作,而send-off是会为每个操作分配一个线程
;定义一个函数可以使用send或send-off来发送操作
(defn exercise-agents [send-fn]
  (let [agents (map #(agent %) (range 15))]
    (doseq [a agents]
      (send-fn a (fn [_] (Thread/sleep 1000))))
    (doseq [a agents]
      (await a))))
;使用send-off,理论上exercise-agents的执行时间只会比1秒多一些,因为这些操作是同时在各自的线程中等待1秒
(time (exercise-agents send-off))
;"Elapsed time: 1007.050541 msecs"
;使用send,因为有线程池,而线程池的核心线程数和CPU核心数有关,当前是8核,所以应该执行的时间应该接近2秒(15 / 8 = 2)
(time (exercise-agents send))
;"Elapsed time: 2007.615091 msecs"

;结论是:send适用于CPU密集型操作,send-off适用于IO密集型操作

;agent的操作有时候会抛出异常,这种情况下,默认是会阻止agent执行接下来的操作的,称为:fail模式
(send log-agent (fn [] 2000))
;可以使用agent-error来返回agent抛出的异常
(agent-error log-agent)
;#error{:cause "Wrong number of args (1) passed to: clojoy.ch10.agent/eval1731/fn--1732",.....
;再发送操作,会报错
(send log-agent (fn [_] 3000))
;Execution error (ArityException) at java.util.concurrent.ThreadPoolExecutor/runWorker (ThreadPoolExecutor.java:1149).
;Wrong number of args (1) passed to: clojoy.ch10.agent/eval1731/fn--1732
@log-agent
;1001
;要重新让agent的操作执行,可以重启agent
(restart-agent log-agent 2500 :clear-actions true)          ;:clear-actions是true则会把为执行的操作清除
;2500
(send-off log-agent do-log "The agent,it lives!")
;2500 : The agent,it lives!
;还有一种错误模式是:continue,这种模式下agent遇到错误还是会继续执行下一个操作
;开启这种模式需要在初始化agent的时候指定:error-handler
(defn handle-log-error [the-agent the-err]
  (println "An action sent to the log-agent threw " the-err))
(def foo (agent 1 :error-handler handle-log-error))
(send foo (fn [] 2))
;An action sent to the log-agent threw  #error {
; :cause Wrong number of args (1) passed to: clojoy.ch10.agent/eval1768/fn--1769
;还可以使用set-error-handler!来修改错误处理器
(set-error-handler! log-agent handle-log-error)
;使用set-error-mode!来修改错误模式
(set-error-mode! log-agent :continue)
(send log-agent (fn [x] (/ x 0)))
;An action sent to the log-agent threw  #error {
; :cause Divide by zero
(send-off log-agent do-log "Stayin' alive,Stayin' alive...")
;2501 : Stayin' alive,Stayin' alive...

;:fail模式也可以调用错误处理器,但是错误处理器不能调用restart-agent,所以在:fail中没有在:continue中有用
