(ns clojoy.ch15.chunked-seq)

;clojure从1.1开始,对于惰性的粒度从单个元素变为一块元素,每个块的大小默认是32
(def gimme #(do (print \.) %))
(take 1 (map gimme (range 32)))
;................................=> (0)
;一共打印了32个"."
;如果把元素数量改成33会怎么样?
(take 1 (map gimme (range 33)))
;................................=> (0)
;还是32个".",这样符合对lazy的预期
(take 1 (drop 32 (map gimme (range 64))))
;................................................................=> (32)
;现在打印了64个"."

;如果想要重新获得一次一个的惰性序列,可以这样做:
(defn seq1
  [s]
  (lazy-seq
   (when-let [[x] (seq s)]
     (cons x (seq1 (rest s))))))
(take 1 (map gimme (seq1 (range 32))))
;.=> (0)
(take 1 (drop 32 (map gimme (seq1 (range 64)))))
;.................................=> (32)

;一般情况下,使用块级惰性序列是没有问题的,除非获取序列的每个元素的花费都非常高昂
