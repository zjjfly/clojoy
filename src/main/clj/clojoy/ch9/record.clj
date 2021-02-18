(ns clojoy.ch9.record)

(defrecord TreeNode [val l r])

(TreeNode. 5 nil nil)
;#clojoy.ch9.record.TreeNode{:val 5, :l nil, :r nil}

(in-ns 'foo)
(clojure.core/refer 'clojure.core)
;如果在其他命名空间中使用deftype或defrecord定义的类,需要使用import
(import 'clojoy.ch9.record.TreeNode)
(TreeNode. 1 2 3)
;#clojoy.ch9.record.TreeNode{:val 1, :l 2, :r 3}
(in-ns 'clojoy.ch9.record)
;record的性能优势:
;1.查找键值的速度相比于map和hash map更快
;2.可以存储原始类型,不需要封装,所以占用的内存更少
;record的缺点:
;1.它本身无法作为函数使用
;2.它无法和map进行比较

(defn xconj [t v]
  (cond
    (nil? t) (TreeNode. v nil nil)
    (< v (:val t)) (TreeNode. (:val t)
                              (xconj (:l t) v)
                              (:r t))
    :else (TreeNode. (:val t) (:l t) (xconj (:r t) v))))

(defn xseq [t]
  (when t
    (concat (xseq (:l t)) [(:val t)] (xseq (:r t)))))

(def sample-tree (reduce xconj nil [3 5 2 4 6]))
(xseq sample-tree)
;(2 3 4 5 6)

;assoc和dissoc可以作用于record
(def node (TreeNode. 1 2 3))
(assoc node :bar 4)
;#clojoy.ch9.record.TreeNode{:val 1, :l 2, :r 3, :bar 4}
(dissoc node :bar)
;#clojoy.ch9.record.TreeNode{:val 1, :l 2, :r 3}
;dissoc如果删除的是record的固有键值,那么返回的是map
(assert (= clojure.lang.PersistentArrayMap (class (dissoc node :l))))
