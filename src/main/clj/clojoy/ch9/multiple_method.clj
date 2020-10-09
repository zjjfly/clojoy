(ns clojoy.ch9.multiple-method
  (:refer-clojure :exclude [get cat])
  (:use [clojoy.utils.assert]))

;实现一个类似JS的原型链的UDP(Universal Design Pattern)
;先定义一个把一个map和一个原型map关联起来的函数
(defn beget [this proto]
  (assoc this ::prototype proto))
(beget {:sub 0} {:super 1})
;{:sub 0, :clojoy.ch9.multiple-method/prototype {:super 1}}
;定义一个get函数,可以在当前map中查找key,如果没找到,会顺着原型链找,就像JS一样
(defn get
  [m k]
  (when m
    (if-let [[_ v] (find m k)]
      v
      (recur (::prototype m) k))))
(get (beget {:sub 0} {:super 1}) :super)
;1
;定义一个put函数,直接复用assoc
(def put assoc)

;使用这个UDP
(def cat {:likes-dogs true :ocd-bathing true})
(def morris (beget {:likes-9lives true} cat ))
(def post-traumatic-morris (beget {:likes-dogs nil} morris))
(get cat :likes-dogs)
;true
(get morris :likes-dogs)
;true
(get post-traumatic-morris :likes-dogs)
;nil

;使用多重方法为UDP添加行为
(defmulti compiler :os)
(defmethod compiler ::unix [m] (get m :c-compiler))
(defmethod compiler ::osx [m] (get m :llvm-compiler))
;定义一些原型map
(def clone (partial beget {}))
(def unix {:os ::unix , :c-compiler "cc" , :home "/home" , :dev "/dev"})
(def osx (-> (clone unix)
             (put :os ::osx)
             (put :llvm-compiler "clang")
             (put :home "/Users")))
(compiler unix)
;"cc"
(compiler osx)
;"clang"
;模拟继承
(defmulti home :os)
(defmethod home ::unix [m] (get m :home))
(home unix)
;"/home"
(assert-error IllegalArgumentException (home osx))
;form throws a java.lang.IllegalArgumentException
; (No method in multimethod 'home' for dispatch value: :clojoy.ch9.multiple-method/osx)
;使用derive定义::osx和::unix的派生关系
(derive ::osx ::unix)
(home osx)
;"/Users"
;用parents,ancestors,descendants和isa?来查询派生层次关系
(parents ::osx)
;#{:clojoy.ch9.multiple-method/unix}
(ancestors ::osx)
;#{:clojoy.ch9.multiple-method/unix}
(descendants ::unix)
;#{:clojoy.ch9.multiple-method/osx}
(isa? ::osx ::unix)
;true
(isa? ::unix ::osx)
;false
(derive ::osx ::bsd)
(defmethod home ::bsd [m] "/home")
;::bsd和::unix都是::osx的直接父级,所以派发函数不知道调用哪一个home函数
(assert-error IllegalArgumentException (home osx))
;form throws a java.lang.IllegalArgumentException
; (Multiple methods in multimethod 'home' match dispatch value: :clojoy.ch9.multiple-method/osx -> :clojoy.ch9.multiple-method/bsd and :clojoy.ch9.multiple-method/unix, and neither is preferred)
;可以使用prefer-method来定义多个派发值的优先顺序
(prefer-method home ::unix ::bsd)
(home osx)
;"/Users"
;使用remove-method可以删除方法
(remove-method home ::bsd)
(home osx)
;"/Users"
;使用derive和make-hierarchy定义派生层次结构
(derive (make-hierarchy) ::osx ::unix)
;{:parents #:clojoy.ch9.multiple-method{:osx #{:clojoy.ch9.multiple-method/unix}},
; :ancestors #:clojoy.ch9.multiple-method{:osx #{:clojoy.ch9.multiple-method/unix}},
; :descendants #:clojoy.ch9.multiple-method{:unix #{:clojoy.ch9.multiple-method/osx}}}

;实现基于多个key的分发
(defmulti compile-cmd (juxt :os compiler))
(defmethod compile-cmd [::osx "clang"] [m]
  (str "/usr/bin/" (get m :llvm-compiler)))
;定义派发值没有对应的方法的时候的默认的方法
(defmethod compile-cmd :default [m]
  (str "Unsure where to locate " (get m :c-compiler)))
(compile-cmd osx)
;"/usr/bin/clang"
(compile-cmd unix)
;"Unsure where to locate cc"

;上面用到的juxt函数可以接受一组函数并组成一个函数,这个函数返回的是一个vector,里面的值是实参应用于每一个函数的结果
(def each-math (juxt + - * /))
(each-math 2 3)
;[5 -1 6 2/3]
((juxt take drop) 3 (range 10))
;[(0 1 2) (3 4 5 6 7 8 9)]
