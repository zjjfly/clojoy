(ns clojoy.ch5.queue
  (:import (clojure.lang PersistentQueue)))

;clojure中有一个PersistentQueue,可以作为队列使用
;它是一个不可变集合,和java.util.concurrent中的队列不同,后者是一个工作流的机制
;如果需要不断观察某个队列,看是否有某项弹出,或者用队列发送一个任务给另一个线程,那么PersistentQueue不符合需求

;clojure的核心库中没有函数用于构造一个队列,所以只能使用现有的一个空队列对象
PersistentQueue/EMPTY
;#object[clojure.lang.PersistentQueue 0x10b0e482 "clojure.lang.PersistentQueue@1"]
;可以看出打印的信息可读性很差,我们自己来定义它的打印信息,只要实现print-method多重方法
(defmethod print-method PersistentQueue
  [q w]
  (print-method '<- w)
  (print-method (seq q) w)
  (print-method '-< w))
PersistentQueue/EMPTY
;<-nil-<

;在一个空队列上pop返回一个空队列
(pop PersistentQueue/EMPTY)
;<-nil-<
;对一个空队列peek返回nil
(peek PersistentQueue/EMPTY)
;nil
;first,rest,next对队列也起作用,但最好还是使用队列专用的函数:pop,peek,conj

;使用conj向队列添加元素
(def schedule
  (conj PersistentQueue/EMPTY
        :wake-up :shower :brush-teeth))
schedule
;<-(:wake-up :shower :brush-teeth)-<

;PersistentQueue的底层是由两个集合构成的,前面是一个seq,后面是一个vector
;所有的插入都发生在后面的vector上,所有的删除都发生在前面的seq上,这就重复利用了两种集合的优势
;当前面的seq的所有项都弹出时,后面的vector就封装成seq成为前端,后的vector作为后端

;从队列拿一个元素,不会在队列中删除这个元素,即不会改变持久化队列的内容
(peek schedule)
;:wake-up
schedule
;<-(:wake-up :shower :brush-teeth)-<

;从队列删除一个元素
(pop schedule)
;<-(:shower :brush-teeth)-<
;虽然是以rest也可以返回相同的结果,但是类型是seq而不是队列
;所以,对于特定的集合,要使用它惯用的操作,这点很重要
