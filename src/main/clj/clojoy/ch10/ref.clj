(ns clojoy.ch10.ref
  (:require [clojoy.ch5.vector :refer [neighbors]])
  (:require [clojoy.utils.concurrency :refer [dothreads!]])
  (:import (clojure.lang Ref)))

;clojure中有四种引用类型:ref,agent,atom和var
;它们的特性都略有不同,下面的列表说明了这个:
;         ref agent atom var
;需要协调   √
;异步           √
;可重试     √         √
;线程局部                  √

;引用类型可以使用纯函数来进行修改,函数结果会作为新的引用值
;还可以使用set-validator来管理一个校验器函数,用于校验每次修改后的新值

;ref允许对其值进行同步的,需要协调的修改
;通过强制让ref的值的改变发生在一个事务中,可以保证所有线程里,看到的引用值都是一致的
;小写字母表示黑子,大写字母表示白子
(def initial-board
  [[:- :k :-]
   [:- :- :-]
   [:- :K :-]
   ])

(defn board-map
  [f board]
  (vec
    (map #(vec (for [s %] (f s)))
         board)))

;初始化棋盘
(defn reset-board!
  []
  (def board (board-map ref initial-board))
  (def to-move (ref [[:K [2 1]] [:k [0 1]]]))               ;表示要移动的顺序,现在是有两个不同颜色的王要移动
  (def num-move (ref 0)))

;计算王的所有相邻的格子
(def king-moves
  (partial neighbors
           [[-1 -1] [-1 0] [-1 1] [0 -1] [0 1] [1 -1] [1 0] [1 1]] 3))

;检查移动是否合法
(defn good-move?
  [to enemy-sq]
  (when (not= to enemy-sq)
    to))

(defn choose-move
  [[[mover mpos] [_ enemy-pos]]]
  [mover (some #(good-move? % enemy-pos)
               (shuffle (king-moves mpos))                  ;把相邻的格子的顺序打乱,模拟随机选取过程
               )])

(reset-board!)
(take 5 (repeatedly #(choose-move @to-move)))
; ([:K (1 2)] [:K (1 1)] [:K (1 0)] [:K (1 2)] [:K (1 0)])

(defn place [from to] to)
;移动之后,更新移动的源格子和目标格子
(defn move-piece [[piece dest] [[_ src] _]]
  (alter (get-in board dest) place piece)
  (alter (get-in board src) place :-)
  (alter num-move inc))

;更新to-move这个ref
(defn update-to-move
  [move]
  (alter to-move #(vector (second %) move)))

(defn make-move
  []
  (let [move (choose-move @to-move)]
    (dosync (move-piece move @to-move))
    (dosync (update-to-move move))
    ))

(reset-board!)

(make-move)
;[[:k [0 1]] [:K (1 1)]]
(board-map deref board)
;[[:- :k :-] [:- :K :-] [:- :- :-]]

(make-move)
;[[:K (1 1)] [:k (1 0)]]
(board-map deref board)
;[[:- :- :-] [:k :K :-] [:- :- :-]]

;看上去没有问题,使用100个线程并发执行
(dothreads! make-move :threads 100 :times 100)
(board-map deref board)
;[[:- :- :-] [:K :- :-] [:- :- :K]]
;显然,某些出现了并发的问题,原因是把对来源格子和目标格子的更新放在不同的事务中了

;clojure的事务性内存(STM)的优点:
;1.无锁,就不会有死锁
;2.不使用monitor,也就不会遗漏唤醒条件
;3.使用多版本并发控制,所以每个事务对其感兴趣的数据都有自己的视图
;4.在事务结束前会检查这个事务中修改的ref的原值是否被其他事务修改了,如修改,这个事务会重试
;5.事务中如果抛出异常,这个事务中的值会被丢弃

;clojure支持嵌套事务,但如果子事务要重试,会引发上一级的事务的重试,事务的提交也是由最外层的事务完成的

;STM不是silver bullet,但确实简化了并发编程的下面几点:
;1.一致性
;2.lock free
;3.ACID中的前三点,D则由用户自己实现
;STM的缺点:
;1.写入偏差,指的是一个事务根据引用的值调整其行为,但不向这个引用写入,另一个事务更新了这个引用的值
;解决方法是使用ensure函数,它可以保证一个事务中的只读ref不会被另一个事务修改
;2.活锁,指的是一套事务重复地一个接一个的重试,Clojure通过两种方式解决这个问题:
;一个是限制重试的次数,还有一个是保证新的事务重试时,老的事务能继续运行

;STM的事务中不推荐存在的操作:
;1.I/O,因为这意味着这个事务函数有副作用,如果事务重试了,可能会产生非预期的结果
;2.类实例变化,原因和上面的一样
;3.大事务,事务应该尽量小,这样可以快进快出,这样事务重试的可能性就会比较小

;修改后的make-move
(defn make-move
  []
  (dosync
    (let [move (choose-move @to-move)]
      (move-piece move @to-move)
      (update-to-move move)
      )))
(reset-board!)
(dothreads! make-move :threads 100 :times 100)
(board-map deref board)
;[[:- :- :-] [:K :- :-] [:- :k :-]]

;如果对某些ref的修改是和当前值无关的,那么可以使用commute函数
(defn move-piece [[piece dest] [[_ src] _]]
  (commute (get-in board dest) place piece)
  (commute (get-in board src) place :-)
  (commute num-move inc))
(reset-board!)
(dothreads! make-move :threads 100 :times 100)
(board-map deref board)
;[[:- :k :-] [:- :- :K] [:- :- :-]]
@num-move
;10000
;commute实际会在提交的时候再次执行修改函数,虽然会有一些额外的损耗,但相比跟踪引用的值,代价还是小了很多

;ref-set可以使用一个新的值代替ref当前的值.一般是当无法对ref进行同步的时候,使用这个函数对其进行修复
(dosync (ref-set num-move 1))
@num-move
;1

;ref不适合用于既有长时间的事务又有短期事务的情况,因为短期事务会不断的修改ref的值,而长期事务在提交的时候就会发现
;ref被修改了,引发重试机制,最终的结果是长期事务会不断地重试而无法结束
(defn stress-ref [^clojure.lang.Ref r]
  (let [slow-tries (atom 0)]
    (future
      (dosync
        (swap! slow-tries inc)
        (Thread/sleep 200)
        @r)
      (println (format "r is: %s, history: %d, after: %d tries"
                       @r (.getHistoryCount r) @slow-tries)))
    (dotimes [i 500]
      (Thread/sleep 10)
      (dosync (alter r inc))
      :done)))
(stress-ref (ref 0))
;r is: 500, history: 10, after: 28 tries
;一种稍微可以减少重试次数的方法是把ref保留的历史值的数量调大,默认是10个
(stress-ref (ref 0 :max-history 30 :min-history 15))
;r is: 73, history: 18, after: 4 tries
;但还是无法避免几次重试
