(ns clojoy.ch3.destructuring)

;最常见的是使用vector进行解构
(def guys-whole-name ["Guy" "Lewis" "Steele"])
(let [[f-name m-name l-name] guys-whole-name]
  (str l-name ", " f-name " " m-name))
;"Steele, Guy Lewis"
;这是一种基于位置的解构,不能用于map或set,因为它们不是顺序集合
;但它可以用于java.util.regex.Matcher
(def date-regex #"(\d{1,2})\/(\d{1,2})\/(\d{4})")
(let [rem (re-matcher date-regex "12/02/1975")]
  (when (.find rem)
    (let [[_ m d] rem]
      {:month m :day d})))
;{:month "12", :day "02"}
;当然,更好的方式是对re-find的返回值进行解构
(let [[_ m d] (re-find date-regex "12/02/1975")]
  {:month m :day d})
;{:month "12", :day "02"}

;vector解构可以使用&来表示把余下的元素放到一个seq中并和&之后的本地变量进行绑定
;还可以使用:as加一个本地变量来对整个集合进行绑定.这不会改变集合的类型,和&是不一样的
;:as必须放在&之后,如果有&的话
(let [range-vec (vec (range 10))
      [a b c & more :as all] range-vec]
  (println "a b c are:" a b c)
  (println "more is:" more)
  (println "all is:" all))
; a b c are: 0 1 2
; more is: (3 4 5 6 7 8 9)
; all is: [0 1 2 3 4 5 6 7 8 9]

;另一种常用的解构是map解构
(def guys-name-map
  {:f-name "Guy" :m-name "Lewis" :l-name "Steele"})

(let [{f-name :f-name, m-name :m-name, l-name :l-name} guys-name-map]
  (str l-name ", " f-name " " m-name))
;"Steele, Guy Lewis"

;可以使用:keys来方便的绑定多个map中的键类型是keyword的值
(let [{:keys [f-name m-name l-name]} guys-name-map]
  (str l-name ", " f-name " " m-name))
;"Steele, Guy Lewis"

;可以使用:strs来方便的绑定多个map中的键类型是string的值
(let [{:strs [a b c]} {"a" 1 "b" 2 "c" 3}]
  (println "a b c are:" a b c))
;a b c are: 1 2 3

;可以使用:syms来方便的绑定多个map中的键类型是symbol的值
(let [{:syms [a b c]} {'a 1 'b 2 'c 3}]
  (println "a b c are:" a b c))
;a b c are: 1 2 3

;map解构也可以使用:as来绑定原始的map
(let [{f-name :f-name, :as whole-name} guys-name-map]
  (println "First name is" f-name)
  (println "Whole name is below:")
  whole-name)
;First name is Guy
;Whole name is below:
;{:f-name "Guy", :m-name "Lewis", :l-name "Steele"}

;如果某个键不存在,可以使用:or来指定这种情况下的缺省值值
(let [{:keys [title f-name m-name l-name],
       :or   {title "Mr."}} guys-name-map]
  (println title f-name m-name l-name))
;Mr. Guy Lewis Steele

;map解构也可以用于解构list,这一特性被函数用来接受关键字参数
(defn whole-name [& args]
  (let [{:keys [f-name m-name l-name]} args]
    (str l-name ", " f-name " " m-name)))
(whole-name :f-name "Guy" :m-name "Lewis" :l-name "Steele")
;"Steele, Guy Lewis"
;上面的args在解构之前会先转成一个map集合

;还有一种解构叫关联解构(associative destructuring)
;它使用和map解构相似的语法来解构vector
(let [{first-thing 0 last-thing 3} [1 2 3 4]]
  [first-thing last-thing])
;[1 4]
;上面的0和3是指元素在vector中的index
