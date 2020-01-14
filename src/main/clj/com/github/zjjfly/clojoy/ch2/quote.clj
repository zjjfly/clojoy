(ns com.github.zjjfly.clojoy.ch2.quote)

;quote是为了防止对一个form进行求值,包括quote和语法quote

;一般的form在求值之前,会对里面的每一项先进行求值,然后把第一项看做函数,使用余下的各项的值作为参数来调用这个函数
;特殊form的写法和一般的form是一样的:以一个符号作为一个list的第一项
;每个特殊form都有自己的求值规则,quote这个特殊form的规则是不对其参数进行求值,而是直接返回
(def age 9)
(quote age)
;age

;无论多复杂的参数都可以做到直接返回
(quote (cons 1 [2 3]))

;使用quote最常见的原因是这样可以用字面量list做数据集合,而不会把它看做一个函数调用
(cons 1 (quote (2 3)))
;(1 2 3)

;quote的快捷方式是在需要quote的form前面加单引号
(cons 1 '(2 3))
;(1 2 3)

;注意1,quote会影响参数中的所有form,不只是最顶层的form,所以这有时候不一定是你想要的
;注意2,空list已经自我求值过了,不需要quote

;语法quote和quote不同的是,它会把它的参数中为限定的符号加上限定
`map
;clojure.core/map
`Integer
;java.lang.Integer
;如果符号命名的var或类不存在,则语法quote会使用当前的命名空间
`is-always-right
;com.github.zjjfly.clojoy.ch2.quote/is-always-right

;有的时候,你想要对语法quote的参数中的某个form进行求值,可以使用反quote,语法是在要求值的form前加一个~
`(+ 10 ~(* 3 2))
;(clojure.core/+ 10 6)
;反quote可以用于任何需要求值的表达式
(let [x 2]
  `(1 ~x 3))
;(1 2 3)
;但如果这个form本身不是可求值的,那么会报错
;`(1 ~(2 3))
;Execution error (ClassCastException) at com.github.zjjfly.clojoy.ch2.quote/eval1662 (quote.clj:1).
;java.lang.Long cannot be cast to clojure.lang.IFn
;一个workaround;
(let [x '(2 3)]
  `(1 ~x))
;(1 (2 3))
;但这种做法的返回结果实际上不是我们想要的(1 2 3)

;解决上面的问题的正确做法是使用反quote拼接
(let [x '(2 3)]
  `(1 ~@x))
;(1 2 3)

;有的时候,我们需要一个随机的符号,例如对参数或let的绑定命名
;在语法quote中,最简单的做法是在符号名后加#,它实际是auto-gensym的简写
`potion#
;potion__1693__auto__
