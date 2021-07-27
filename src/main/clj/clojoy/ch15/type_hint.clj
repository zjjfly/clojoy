(ns clojoy.ch15.type-hint)

;clojure的类型提示有助于提高性能,但原则是:先实现函数功能,后提高性能

;开启clojure的反射警告,这样编译器会在无法直接推导出对象的类型而需要使用反射的时候发出警告
;这可以帮助我们知道哪些地方最需要加上类型提示
(set! *warn-on-reflection* true)

;计算数组的平方和的方法,会有很多反射⚠️
(defn asum-sq [xs]
  (let [dbl (amap xs i ret
                  (* (aget xs i)
                     (aget xs i)))]
    (areduce dbl i ret 0
             (+ ret (aget dbl i)))))
;测试一下它的性能
(time (dotimes [_ 10000]
        (asum-sq (into-array [1 2 3 4 5]))))
;"Elapsed time: 1369.843402 msecs"

;加上类型提示
(defn asum-sq [^floats xs]
  (let [^floats dbl (amap xs i ret
                          (* (aget xs i)
                             (aget xs i)))]
    (areduce dbl i ret 0.0
             (+ ret (aget dbl i)))))
(time (dotimes [_ 10000]
        (asum-sq (float-array [1 2 3 4 5]))))
;"Elapsed time: 8.056899 msecs"

;调用Java方法
(.intValue (asum-sq (float-array [1 2 3 4 5])))
;Reflection warning, reference to field intValue can't be resolved.
;55
;可以看出还是有反射警告,是因为编译器无法推断函数的返回值类型,可以在给函数加上返回值的类型提示
(defn ^Double asum-sq [^floats xs]
  (let [^floats dbl (amap xs i ret
                          (* (aget xs i)
                             (aget xs i)))]
    (areduce dbl i ret 0.0
             (+ ret (aget dbl i)))))
(.intValue (asum-sq (float-array [1 2 3 4 5])))
;55
