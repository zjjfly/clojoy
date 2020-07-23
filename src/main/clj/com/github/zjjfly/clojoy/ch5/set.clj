(ns com.github.zjjfly.clojoy.ch5.set
  (:require [clojure.set :as cset]))

;clojure的set和数学意义上的set一致,其中的元素无序且唯一
;它可以作为函数,测试某个值是否在set中,如果不在返回nil
(#{:a :b :c :d} :c)
;:c
(#{:a :b :c :d} :e)
;nil

;set的元素可以通过get访问,如果查询值存在于结合,则返回它
(get #{:a 1 :b 2} :b)
;:b
;使用get的额外好处是,可以指定默认值
(get #{:a 1 :b 2} :z :nothing-doing)
;:nothing-doing

;只要两个元素的求值结果是相等的,那么set中只会包含其中一个,和具体类型无关
(into #{[]} [()])
;#{()}
(into #{[1 2]} '[(1 2)])
;#{[1 2]}
(into #{[] #{} {}} [()])
;#{[] #{} {}}
;最后一个例子可以看出,在一个划分里的合集,只要其中元素相等,它们的值就是相等的;如果不在一个划分里,即使元素相等,它们的值仍不算相等

;之前提到,无法用contains来判断vector中是否包含特定的值,但使用set可以实现这个功能
(some #{:b} [:a 1 :b 2])
;:b
;这是一种惯用手法,常用于搜索在序列中是否包含特定的值

;有序的set
(sorted-set :b :c :a)
;#{:a :b :c}
(sorted-set [3 4] [1 2])
;#{[1 2] [3 4]}
;sorted-set接受的参数之间必须是可以比较的,否则会报错
(try
  (sorted-set :a 1)
  (catch Exception e
    (assert (not (nil? e)))))
;这一点在使用已有的sorted set的时候会很明显的显示出来
(def my-set (sorted-set :a :b))
(try
  (conj my-set 1)
  (catch Exception e
    (.getMessage e)))
;java.lang.ClassCastException: clojure.lang.Keyword cannot be cast to java.lang.Number
;这个错误会让人很困惑,特别是当sorted set的生成和操作在代码中离的比较远的时候
;要防止这个错误可以使用sorted-set-by,提供自己的比较器,sorted-map-by的作用也是类似的

;contains?的行为和java.util.Collection#contains是不一样的,它是检测集合中是否包含给定的key,而key在不同集合中有不同含义
;key在map中和其他语言没什么区别,但对于clojure的set,key和元素是一致的,所以contains?的结果不会有问题
(contains? #{1 2 4 3} 4)
;true
;但key对于clojure的vector,key是索引,所以contains?的结果是有问题的
(contains? [1 2 4 3] 4)
;false

;clojure.set命名空间下的一些函数,它们都可以接受多个set作为参数.首先会对前两个set进行计算,得到的结果和第三个set进行计算,以此类推
;intersection,返回多个集合的交集
(cset/intersection #{:humans :fruit-bats :zombies}
                   #{:chupacabra :zombies :humans})
;#{:zombies :humans}
(cset/intersection #{:pez :gum :dots :skor}
                   #{:pez :skor :pocky}
                   #{:pocky :gum :skor})
;#{:skor}

;union,返回多个集合的交集
(cset/union #{:humans :fruit-bats :zombies}
                   #{:chupacabra :zombies :humans})
;#{:chupacabra :zombies :humans :fruit-bats}
(cset/union #{:pez :gum :dots :skor}
            #{:pez :skor :pocky}
            #{:pocky :gum :skor})
;#{:pocky :pez :skor :dots :gum}

;difference,返回一个set,它是由第一个set删除所有也出现在第二个set中的元素得到的
(cset/difference #{1 2 3 4} #{3 4 5 6} #{1})
;#{2}
