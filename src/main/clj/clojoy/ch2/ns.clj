(ns clojoy.ch2.ns
  (:require [clojure.string :as string :refer [capitalize trim] :rename {trim strip}])
  ;clojure的最佳实践是显示的生成refer的var,就像上面这样,不推荐使用:use
  ;这样就不会在当前的命名空间生成很多不需要的名称,否则容易引起命名冲突,并且让代码的可读性变糟糕
  (:refer clojure.set :rename {union onion})
  ;refer的作用和require中的:refer类似,但只对已经加载的ns起作用
  ;可以使用:rename对其中的var加别名,这一般用于避免和当前的命名空间的var发生命名冲突,它也可以在:require中使用
)

(capitalize "clojure")
;Clojure

(strip " clojure ")
;clojure
