(ns com.github.zjjfly.clojoy.ch4.number)

;clojure对过长的浮点数默认会截断
(let [butieatedit 3.14159265358979323846264338327950288419716939937]
  (println (class butieatedit))
  butieatedit)
;java.lang.Double
;3.141592653589793

;如果需要高精度,要显示声明字面量类型,但这种做法也不能保证准确无误的精度
(let [imadeuapi 3.14159265358979323846264338327950288419716939937M]
  (println (class imadeuapi))
  imadeuapi)
;java.math.BigDecimal
;3.14159265358979323846264338327950288419716939937M

;clojure会在发生上溢时检测到并进行自动的提升来容纳这个值
(def clueless 9)

(class clueless)
;java.lang.Long

(class (+ clueless 9000000000000000000000000))
;clojure.lang.BigInt

(class (+ clueless 0.9))
;java.lang.Double

;clojure在处理原生类型的数字运算是,会发生上溢,和Java不同的是,clojure不会让它折回,而是抛出异常
(+ Long/MAX_VALUE 1)
;ArithmeticException:integer overflow

;clojure提供了一些不受控的函数来避免抛出溢出异常
(unchecked-add Long/MAX_VALUE 1)
;-9223372036854775808

;一个数值非常小的时候,会变成0,这叫做下溢,只出现在浮点数上
(float 0.0000000000000000000000000000000000000000000000000000001)
;0.0
1E-400
;0.0

;当浮点值的表现形式不足以存储实际的值的时候,会发生舍入错误,它是一个比较隐蔽的错误,而且会随着运算次数的增加累计
;下面是著名的爱国者导弹的一个因为舍入错误的bug
(let [approx-interval (/ 209715 2097152)
      actual-interval (/ 1 10)
      hours (* 3600 100 10)
      actual-total (double (* hours actual-interval))
      approx-total (double (* hours approx-interval))]
  (- actual-total approx-total))
;0.34332275390625
;可以看到在100小时之后偏差累计到0.34秒,这在有些地方是致命的
;在和Java互操作的时候比较容易出现这个错误

;clojure中引入了一个有理数类型,可以很好的解决浮点数存在的问题
;浮点数的问题一个是由于底层表示方法的问题导致越小的数越可能无法表示而只能取近似值,还有一个是不具备结合性和分配性
(def a 1.0E50)
(def b -1.0E50)
(def c 17.0E00)
(+ (+ a b) c)
;17.0
(+ a (+ b c))
;0.0
;可以看出,只是用括号改变了计算顺序,结果就不一样了
;使用有理数就可以避免这个问题
(def a (rationalize 1.0E50))
(def b (rationalize -1.0E50))
(def c (rationalize 17.0E00))
(+ (+ a b) c)
;17N
(+ a (+ b c))
;17N

;为了在计算中准确无误的保持数的精度,有几点要注意:
;1.不要使用java math库,除非其结果是BigDecimal,即使是,也值得怀疑
;2.不要用rationalize处理java float和double的原生值
;3.如果必须写高精度计算,使用有理数
;4.只在万不得已才转成浮点数表示形式

;clojure提供了方法抽出有理数的分子和分母
(numerator (/ 123 10))
;123
(denominator (/ 123 10))
;10

;有理数的缺点是性能不高,如果速度是大于精度的考量,则可以使用原生类型
