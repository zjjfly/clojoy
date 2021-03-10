(ns clojoy.ch11.future
  (:require [clojure [xml :as xml]
             [zip :as zip]]
            [clojure.java.io :as io])
  (:import (java.util.regex Pattern)))

;future可以生成一个Future,它会在其他线程中执行,如果在其未完成的时候解引用,会阻塞
(time (let [x (future (do (Thread/sleep 1000) (+ 1 1)))]
        [@x @x]))
;"Elapsed time: 1000.429149 msecs"

;实现一个简单的计算某个单词在特定的RSS和Atom的文章标题中的出现频率
(defn feed->zipper [uri-str]
  (->> (xml/parse uri-str)
       zip/xml-zip))

(defn normalize [feed]
  (if (= :feed (:tag (first feed)))
    feed
    (zip/down feed)))

(defn feed-children [uri-str]
  (->> uri-str
       feed->zipper
       normalize
       zip/children
       (filter (comp #{:item :entry} :tag))))

(defn title [entry]
  (some->> entry
           :content
           (some #(when (= :title (:tag %)) %))
           :content
           first))

(defn count-text-task [extractor txt feed]
  (let [items (feed-children feed)
        re (Pattern/compile (str "(?i)" txt))]
    (->> items
         (map extractor)
         (mapcat #(re-seq re %))
         count)))

(count-text-task
 title
 "Erlang"
 "http://feeds.feedburner.com/ElixirLang")
;0
(count-text-task
 title
 "Elixir"
 "http://feeds.feedburner.com/ElixirLang")
;41

;使用future实现多个URI的并行统计
(def feeds #{"http://feeds.feedburner.com/ElixirLang"
             "http://blog.fogus.me/feed/"})
(let [results (for [feed feeds]
                (future (count-text-task title "and" feed)))]
  (reduce + (map deref results)))

;使用宏来实现一个通用的把一个输入的seq分成多个future去并行处理,并在最后汇聚成结果
(defmacro as-futures [[a args] & body]
  (let [parts (partition-by #{'=>} body)
        [acts _ [res]] (partition-by #{:as} (first parts))
        [_ _ task] parts]
    `(let [~res (for [~a ~args] (future ~@acts))]
       ~@task)))

;使用这个宏来实现之前的计算词频的函数
(defn occurrences [extractor tag & feeds]
  (as-futures [feed feeds]
              (count-text-task extractor tag feed)
              :as results
              =>
              (reduce + (map deref results))))
(occurrences title "released"
             "http://feeds.feedburner.com/ElixirLang"
             "http://blog.fogus.me/feed/"
             "http://www.ruby-lang.org/en/feeds/news.rss")
;33

(def f (future (Thread/sleep 3000) 1))
(future-done? f)
;false
(future-cancel f)
;true
(future-cancelled? f)
;true
