(ns com.github.zjjfly.clojoy.ch8.macro3)

;clojure中的符号对应的值取决于是否是全限定的,它的解析的时机,它的词法上下文
;这些都可以通过quote和反quote来控制

;宏系统中一个潜在的问题是name capture,它指的是在编译的时候生成的变量名和运行时的变量名发生冲突
;clojure基本不会有这个问题,因为语法quote在宏展开的时候去解析符号,也就是指向当前命名空间中的var
;而不是去无法确定的指向上下文中查找,这减少了复杂性
(defmacro resolution [] `x)
(macroexpand '(resolution))
;com.github.zjjfly.clojoy.ch8.macro3/x
;下面的代码对于Clojure是没问题的,但对于一些较差的语法quote实现是有问题的
(def x 9)
(let [x 109] (resolution))
;9

;Clojure提供了一种把符号解析成函数的本地变量的方法,叫做selective name capture
;Anaphora指的是在口语中一个指向句子中之前出现的主语或宾语的代词,例如them,it这类词,主要是为了减少句子中的重复
;在编程语言中的例子就是Scala的_,Clojure没有引入这个特性,因为它不能嵌套
;但我们可以使用宏来实现anaphora,以Arc中的awhen为例子
(defmacro awhen [exp & body]
  `(let [~'it ~exp] ;~'在语法quote中可以避免被解析为全限定名的var,这就叫selective name capture
    (when ~'it
      ~@body)))
(awhen [1 2 3] (it 2))
;3
(awhen nil (println "Will never get here"))
;nil
(awhen 1 (awhen 2 [it]))
;[2]
;awhen类似Clojure中的if-let和when-let
;只有一种情况会用到selective name capture,那就是你要在你的宏中用到第三方的宏或函数,且这些宏或函数对现有的anaphora有依赖
;一个例子是proxy这个宏
