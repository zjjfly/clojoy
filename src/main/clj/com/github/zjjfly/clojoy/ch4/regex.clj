(ns com.github.zjjfly.clojoy.ch4.regex)

;java的正则表达式足够强大,所以clojure直接拿来用了
;clojure加了正则表达式字面量和一些函数,使得java的正则表达式可以和clojure更好的协作

;字面量,和字符串很像,只是多了一个前缀#
(class #"a pattern")
;java.util.regex.Pattern

;clojure的正则字面量中引号的内容的转义方式和java不同,不需要加转义符\
(re-find #"\d+" "123")
;"123"

;正则可以加上一些标志,来控制匹配行为.写法是在字符串开头加(?<flag>)
;如忽略大小写
(re-find #"(?i)yo" "yO")
;"yO"

;re-seq函数返回一个包含了所有匹配项的惰性seq,这使得它可以用于if判断和找出所有匹配
(re-seq #"\w+" "one-two/three")
;("one" "two" "three")
;如果正则表达式有捕获组,那么返回的seq中的元素的类型是vector,这个vector包含了捕获组从0到最后的所有结果
(re-seq #"\w*(\w)" "one-two/three")

;Java的正则引擎中有一个Matcher的对象,在它遍历字符串寻找匹配的时候,可能会以非线程安全的方法进行改变
;所以,尽量不要直接使用Matcher对象,所以最好也不要使用re-find,re-group这两个函数的单参数形式
