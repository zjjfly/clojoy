(ns clojoy.ch4.symbol)

;符号和其他语言中的标识符类似,但它可以直接引用,也可以通过使用quote或'特殊运算符引用.
;和关键字不同,符号不会只根据名字判定
(identical? 'goat 'goat)
;false,原因是这两个符号实际是两个clojure.lang.Symbol对象,只是碰巧名字相同而已

;但名字相同是符号相等的基础
(= 'goat 'goat)
;true
(name 'goat)
;"goat"

;如果两个符号指向的是同一个对象时,identical?才会返回true
(let [x 'goat
      y x]
  (identical? x y))
;true

;为什么不像关键字一样让相同名称的符号指向同一对象?,因为clojure的对象存在着元数据,除了关键字
;使用with-meta来给对象添加元数据
(let [x (with-meta 'goat {:ornery true})
      y (with-meta 'goat {:ornery false})]
  [(= x y)
   (identical? x y)
   (meta x)
   (meta y)])
;[true false {:ornery true} {:ornery false}]
;可以看出符号的相等性不依赖于元数据,这一点适用于其他的clojure对象.

;符号和关键字一样,不和特定的命名空间绑定
(def a-symbol 'where-am-i)
a-symbol
;where-am-i
(resolve 'a-symbol)
;#'clojoy.ch4.symbol/a-symbol
`a-symbol
;clojoy.ch4.symbol/a-symbol
;使用resolve或语法quote返回结果是包含了命名空间,但这只是求值的需要,而不是符号固有的属性,这也适用于类名限定的符号

;clojure是一种lisp-1,因为它对函数和值绑定使用相同的名字解析机制
