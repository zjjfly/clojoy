(ns clojoy.ch9.namespace
  (:refer-clojure :exclude [+]))

;命名空间可以看成是两次映射,一是符号到命名空间的映射,二是命名空间到var的映射

;创建命名空间的三种方式:
;1.ns
;如果没有这个命名空间,ns会创建一个,并切换至这个命名空间,一般用于源码开头
(ns chimp)
;ns会引入java.lang包中的所有类以及clojure.core中的函数,宏和特殊form
(reduce + [1 2 (Integer. 3)])
;6

;2.in-ns
;它也会像ns一样导入java.lang包,但它不会引入clojure.core中的函数,宏
;它可以显式的接受一个符号作为命名空间限定符
(in-ns 'gibbon)
;#object[clojure.lang.Namespace 0x54200fc "gibbon"]
;下面的代码会报错,因为reduce在clojure.core中,还没有被导入
;(reduce + [1 2 (Integer. 3)])
;Syntax error compiling at (namespace.clj:19:1).Unable to resolve symbol: reduce in this context
;Unable to resolve symbol: reduce in this context
(clojure.core/refer 'clojure.core)
(reduce + [1 2 (Integer. 3)])
;6

;3.create-ns
;它是创建命名空间的最细粒度的方式,传入一个符号,返回一个命名空间对象
(def b (create-ns 'bonobo))
b
;#object[clojure.lang.Namespace 0x7af17f "bonobo"]
((ns-map b) 'String)
;java.lang.String
;ns-map返回的是一个命名空间中的所以绑定关系
;添加和删除绑某个符号的绑定
;intern用于在命名空间中查找符号,如果不存在,创建一个新的
(intern b 'x 9)
;#'bonobo/x
bonobo/x
;9
;向绑定中加入clojure.core中的函数
(intern b 'reduce clojure.core/reduce)
(intern b '+ clojure.core/+)
(in-ns 'bonobo)
(reduce + [1 2 3])
;6
;ns-unmap可以移除命名空间中特定的绑定
(in-ns 'clojoy.ch9.namespace)
(ns-unmap 'bonobo 'reduce)
((ns-map 'bonobo) 'reduce)
;nil
;使用remove-ns可以删除命名空间
(remove-ns 'bonobo)
;列出所有的命名空间
(all-ns)
;使用intern和ns-unmap的时候要谨慎!
;以下是实验把clojure.core加入到create-ns产生的命名空间里
(def n (create-ns 'new-ns))
(doseq [[k v] (merge (ns-map n) (ns-map 'clojure.core))]
  (intern n k v))
(in-ns 'new-ns)
(+ 1 2)
(remove-ns 'new-ns)

(in-ns 'clojoy.ch9.namespace)
;定义命名空间私有的var,通过在元数据中加入:private true这一对键值
(defn- foo [] [])
(def ^:private s "xxx")
(defmacro ^:private bar [] [])
(in-ns 'banobo)
(clojoy.ch9.namespace/foo)

;在ns宏中,可以使用:require :use :import :load这样的关键字细粒度的控制引入这个命名空间中的绑定
(ns joy-ex
  (:refer-clojure :exclude [defstruct])   ;排除clojure.core中的某些绑定
  (:use (clojure set xml))                ;把某个命名空间中的绑定复制到当前的命名空间,这种做法最好避免,因为会产生名称污染
  (:use [clojure.test :only (are is)])    ;只复制某些绑定
  (:require (clojure [zip :as z]))        ;和use类似,但是复制过来的符号带有命名空间前缀,或者使用:as指定一个前缀
  (:import (java.util Date)
           (java.io File)))
