(ns clojoy.ch17.debugging
  (:require [clojure.main :as m]
            [clojoy.ch8.macro1 :refer [context-eval]]))

;实现debug断点

;repl read hook
(defn readr [prompt exit-code]
  (let [input (m/repl-read prompt exit-code)]
    (if (= input ::tl)
      exit-code
      input)))

(comment
  (readr #(print "invisible=> ") ::exit)
  [1 2 3] ;; this is what you type
  ;;=> [1 2 3]

  (readr #(print "invisible=> ") ::exit)
  ;; ::tl ;; this is what you type
  ;;=> :joy.debugging/exit
  )

;一个保存上下文到&env中的宏
(defmacro local-context []
  (let [symbols (keys &env)]
    (zipmap (map (fn [sym] `(quote ~sym))
                 symbols)
            symbols)))

(local-context)
;{}

(let [a 1 b 2 c 3]
  (let [b 200]
    (local-context)))
;{a 1, b 200, c 3}

;断点宏
(defmacro break []
  `(m/repl
     :prompt #(print "debug=> ")
     :read readr
     :eval (partial context-eval (local-context))))

(comment
  (defn div [n d] (break) (int (/ n d)))
  (div 10 0)

  ;debug=> n
  ;;=> 10

  ;debug=> d
  ;;=> 0

  ;debug=> (local-context)
  ;;=> {n 10, d 0}

  ;debug=> ::tl
  ;; ArithmeticException Divide by zero
  )

;可以使用多个断点,因为可以使用::tl退出当前断点
(defn keys-apply [f ks m]
  (break)
  (let [only (select-keys m ks)]
    (break)
    (zipmap (keys only) (map f (vals only)))))

(comment
  (keys-apply inc [:a :b] {:a 1, :b 2, :c 3})

  ;debug=> only
  ;; java.lang.RuntimeException: Unable to resolve symbol: only in this context

  ;debug=> ks
  ;;=> [:a :b]

  ;debug=> m
  ;;=> {:a 1, :b 2, :c 3}

  ;debug=> ::tl
  ;debug=> only
  ;;=> {a 1, :b 2}

  ;debug=> ::tl
  ;;=> {:a 2, :b 3}
  )

;在宏中也可以使用断点宏
(defmacro awhen [expr & body]
  (break)
  `(let [~'it ~expr]
     (if ~'it
       (do (break) ~@body))))

(comment
  (awhen [1 2 3] (it 2))

  ;debug=> it
  ;; java.lang.RuntimeException: Unable to resolve symbol: it in this context

  ;debug=> expr
  ;;=> [1 2 3]

  ;debug=> body
  ;;=> ((it 2))

  ;debug=> ::tl
  ;debug=> it
  ;;=> [1 2 3]

  ;debug=> (it 1)
  ;;=>  2

  ;debug=> ::tl
  ;;=> 3
  )
