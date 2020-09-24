(ns com.github.zjjfly.clojoy.ch8.macro4
  (:import [java.io BufferedReader InputStreamReader IOException]
           [java.net URL])
  (:require [com.github.zjjfly.clojoy.utils.assert :as assert]))

;宏的一个使用场景是帮助用户管理资源,典型的是with-open这个宏

(defn clj-doc-www
  ^BufferedReader []
  (-> "https://clojuredocs.org"
      (URL.)
      .openStream
      InputStreamReader.
      BufferedReader.))
(let [stream (clj-doc-www)]
  (with-open [page stream]
    (println (.readLine page))
    (print "The stream will now close... "))
  ;这里再调用readLine会报错,因为stream已经close了
  (println "but let's read from it anyway.")
  (assert/assert-error IOException (.readLine stream)))

;有的时候,资源的是否操作不一定是close方法,所以我们写一个更通用的宏,可以接受一个是否资源的函数
(defmacro with-resource [binding close-fn & body]
  `(let ~binding
     (try
       (do ~@body)
       (finally
         (~close-fn ~(binding 0))))))

(let [stream (clj-doc-www)]
  (with-resource [page stream]
                 .close
                 (.readLine page)))
;"<!DOCTYPE html>"
;可以看出,这两个宏通过让用户显示的传递一个绑定vector来避免使用anaphora,这种方式在clojure中很普遍

;把本章的内容合到一起,实现一个用于生成第七章的vegan-constraints这样的函数的宏contract
;(contract doubler
;  [x]
;  (:require
;    (pos? x))
;  (:ensure
;    (= (* 2 x) %)))
;上面的代码在预期中会定义一个对于函数的约束,这个函数有一个参数,必须是正数,函数的返回值必须是输入的两倍

(declare collect-bodies)
(defmacro contract
  [name & forms]
  (list 'def name (list* `fn (collect-bodies forms))))

;contract是一个入口宏,它需要返回的form是这样的:
;(fn doubler
;  ([f x]
;     {:post [(= (* 2 x) %)],
;      :pre [(pos? x)]}
;(f x)))
(declare build-contract)
(defn collect-bodies
  [forms]
  (for [form (partition 3 forms)]
    (build-contract form)))
(defn build-contract
  [c]
  (let [args (first c)]
    (list
      (into '[f] args)
      (apply merge
             (for [con (rest c)]
               (cond
                 (= (first con) :require) (assoc {} :pre (vec (rest con)))
                 (= (first con) :ensure) (assoc {} :post (vec (rest con)))
                 :else (throw (Exception.
                                (str "Unknown tag "
                                     (first con)))))))
      (list* 'f args))))

(contract doubler
          [x]
          (:require
            (pos? x))
          (:ensure
            (= (* 2 x) %)))
(def times2 (partial doubler #(* 2 %)))
(times2 2)
;4
(def times3 (partial doubler #(* 3 %)))
(assert/assert-error AssertionError (times3 2))
;form throws a java.lang.AssertionError(Assert failed: (= (* 2 x) %))
(contract doubler
          [x]
          (:require
            (pos? x))
          (:ensure
            (= (* 2 x) %))
          [x y]
          (:require
            (pos? x)
            (pos? y))
          (:ensure
            (= (* 2 (+ x y)) %)))
((partial doubler #(* 2 (+ %1 %2))) 2 3)
;10
((partial doubler #(+ %1 %1 %2 %2)) 2 3)
;10
(assert/assert-error AssertionError ((partial doubler #(* 3 (+ %1 %2))) 2 3))
;form throws a java.lang.AssertionError(Assert failed: (= (* 2 (+ x y)) %))
