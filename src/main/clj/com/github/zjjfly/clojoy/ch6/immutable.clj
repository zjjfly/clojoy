(ns com.github.zjjfly.clojoy.ch6.immutable)

;clojure的list的底层使用了结构共享技术
(def baselist (list :barname :adam))
(def lst1 (cons :willie baselist))
(def lst2 (cons :phoenix baselist))

;可以把baselist看成是lst1和lst2的历史版本,它也是两个list的共享部分
;它们的next部分不仅在值上相等,而且实际是同一个list
(= (next lst1) (next lst2))
;true
(identical? (next lst1) (next lst2))
;true

;vector和map的也使用了结构共享,但底层的数据结构是树
;自己实现一个共享结构的conj方法,树的左分支存放小于树的:val的值,其他的存放在右分支
(defn xconj [t v]
  (cond
    (nil? t) {:val v,:L nil,:R nil}
    (< v (:val t)) {:val (:val t)
                    :L (xconj (:L t) v)
                    :R (:R t)}
    :else {:val (:val t)
           :L (:L t)
           :R (xconj (:R t) v)}
    ))
(def tree1 (xconj nil 5))
tree1
;{:val 5, :L nil, :R nil}
(def tree1 (xconj tree1 3))
tree1
;{:val 5, :L {:val 3, :L nil, :R nil}, :R nil}
(def tree1 (xconj tree1 2))
tree1
;{:val 5, :L {:val 3, :L {:val 2, :L nil, :R nil}, :R nil}, :R nil}
;打印出的结果可读性不好,自定义一个把树转成seq的函数
(defn xseq [t]
  (when t
    (concat (xseq (:L t)) [(:val t)] (xseq (:R t)))))
(xseq tree1)
;(2 3 5)
(def tree2 (xconj tree1 7))
(xseq tree2)
;(2 3 5 7)
