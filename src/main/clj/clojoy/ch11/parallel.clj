(ns clojoy.ch11.parallel
  (:require [clojure.core.reducers :as r]))

;clojure内置的一些函数提供了和as-futures,with-promises类似的功能

;pvalues类似于as-futures,可以并行执行任意数量的表达式,返回一个lazy seq
(pvalues 1 2 (+ 1 2))
;(1 2 3)

(defn sleeper [s thing] (Thread/sleep (* 1000 s)) thing)
(defn pvs [] (pvalues
              (sleeper 2 :1st)
              (sleeper 3 :2nd)
              (keyword "3rd")))
;pvalues返回的是lazy seq,所以它的问题是,获取其中任意的一个元素,时间都是和前面元素中时间成本最昂贵的元素一样多
(-> (pvs) first time)
;"Elapsed time: 2004.709424 msecs"
(-> (pvs) last time)
;"Elapsed time: 3004.580567 msecs"

;pmap是map的并行版本
(->> [1 2 3]
     (pmap (comp inc (partial sleeper 2)))
     doall
     time)
;"Elapsed time: 2004.709424 msecs"
;不要无脑把map改成pmap,因为pmap内部的需要一定的协调成本,只有确定函数执行的成本大于这个成本的时候才适合使用pmap

;pcalls接受任意数量的无参数函数,并行的执行它们,并把它们的执行结果放入一个lazy seq
(-> (pcalls
     #(sleeper 2 :1st)
     #(sleeper 3 :2nd)
     #(keyword "3rd"))
    doall
    time)
;"Elapsed time: 3001.595472 msecs"


;fold是reduce的并行版本,但要求函数是和顺序无关的
(def big-vec (vec (range (* 1000 1000))))

(time (reduce + big-vec))
;"Elapsed time: 5.821226 msecs"
;=>499999500000

(time (r/fold + big-vec))
;"Elapsed time: 3.285875 msecs"
;=>499999500000
