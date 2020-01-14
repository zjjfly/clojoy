(ns com.github.zjjfly.clojoy.ch2.data-type
  (:import (java.awt Point)))

;整数字面量可以使用基数记法,格式是以数字加上字符r开头,后面跟着具体数字
;最大的基数是36,因为数字和字母总共只有36个,
32r3v
;对于8进制和16进制整数,可以使用更简单表示法,同Java中一样
;8进制
0177
;127
0x7f
;127

;clojure支持有理数
(class 1/2)
;clojure.lang.Ratio
;如果有可能,clojure会简化有理数
100/4
;25

;关键字字面量的语法以:开头,在clojure中使用更多的是关键字而非符号
;但:不属于关键字名称的的一部分
(name :a)
;"a"
