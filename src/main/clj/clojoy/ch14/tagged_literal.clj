(ns clojoy.ch14.tagged-literal
  (:use [clojoy.ch7.recursive :only [convert]])
  (:require [clojure.edn :as edn]))

;tagged literal是clojure 1.4引入了的新特性,让我们可以方便的声明某些复杂类型的字面量
;下面的日期的tagged literal
#inst "2021-01-02"
; #inst"2021-01-02T00:00:00.000-00:00"
(class #inst "2021-01-02")
;java.util.Date

;这个特性也可以在clojurescript中使用,语法是一样的,只是转换成js内置的日期类型的实例

;这个特性支持自定义解析器
(def distance-reader
  (partial convert
           {:m 1
            :km 1000
            :cm 1/100
            :mm [1/10 :cm]}))

;使用这个解析器,需要动态的绑定*data-readers*,它是一个map,注册标记对应的解析器
(binding [*data-readers* {'unit/length distance-reader}]
  (read-string "#unit/length [1 :km]"))
;1000

;clojure1.7加入了一个*default-data-readers-fn*,用于tag没有在*data-readers*注册的情况的下的默认解析器
(binding [*default-data-reader-fn* #(-> {:tag %1 :payload %2})]
  (read-string "#nope [:foo]"))
;{:tag nope, :payload [:foo]}

;这个特性存在安全性问题,因为它会执行输入的form,如果这个form会执行恶意代码,那会造成灾难
;所以尽量只用它读取受信任的数据源的输入,对于其他的数据源,推荐使用edn
;end是clojure1.5引入的新的一种格式,并附带了配套的解析器
;它也可以解析tagged literal
(edn/read-string "#uuid \"dae78a90-d491-11e2-8b8b-0800200c8a66\"")
;#uuid"dae78a90-d491-11e2-8b8b-0800200c8a66"

;它还可以解析其他类型
(edn/read-string "12")
;12
(edn/read-string "{:a 42,\"b\" 36,[:c] 9}")
;{:a 42, "b" 36, [:c] 9}

;对于自定义标签,需要在调用read-string的时候指定一个处理特定标签的读取器
(def T {'unit/length distance-reader})
(edn/read-string {:readers T} "#unit/length [1 :km]")
;1000
;还可以指定一个默认的读取器处理没有找到读取器的标签
(edn/read-string {:readers T :default vector} "#what/the :huh?")
;[what/the :huh?]

;实现一个产生宏的宏,产生的宏可以转换一个距离到标准距离(按米计)
(defn relative-unit [context unit]
  (if-let [spec (get context unit)]
    (if (vector? spec)
      (convert context spec)
      spec)
    (throw (RuntimeException. (str "Undefined unit " unit)))))
(relative-unit {:m 1
                :km 1000
                :cm 1/100
                :mm [1/10 :cm]}
               :km)
;1000
(defmacro defunit-of [name base-unit & convertions]
  (let [magnitude (gensym)
        unit (gensym)
        units-map (into `{~base-unit 1}
                        (map vec (partition 2 convertions)))]
    `(defmacro ~(symbol (str "unit-of-" name))
       [~magnitude ~unit]
       `(* ~~magnitude
           ~(case ~unit
              ~@(mapcat
                 (fn [[u# & r#]]
                   `[~u# ~(relative-unit units-map u#)])
                 units-map))))))
(defunit-of distance :m
  :km 1000
  :cm 1/100
  :mm [1/10 :cm]
  :ft 0.3048
  :mile [5280 :ft])
;上面的代码等同下面的这段:
(defmacro real-unit-of-instance
  [magnitude unit]
  `(* ~magnitude
      ~(case unit
         :mile 1609.344
         :km 1000
         :cm 1/100
         :m 1
         :mm 1/1000
         :ft 0.3048)))
(macroexpand '(unit-of-distance 1 :cm))
;(clojure.core/* 1 1/100)
(macroexpand '(real-unit-of-instance 1 :cm))
;(clojure.core/* 1 1/100)
