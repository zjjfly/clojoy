(ns clojoy.ch17.test
  (:require [clojoy.ch11.future :as joy]
            [clojure.test :refer [deftest testing is run-tests]]
            [clojoy.ch8.macro4 :refer [contract]]
            ))

;clojure可以很方便的创建测试桩
(def stubbed-feed-children
  (constantly [{:content [{:tag     :title
                           :content ["Stub"]}]}]))

(defn count-feed-entities [url]
  (count (joy/feed-children url)))

(count-feed-entities "http://blog.fogus.me/feed/")
;5

;with-redefs
(with-redefs [joy/feed-children stubbed-feed-children]
  (count-feed-entities "dummy url"))
;1

(with-redefs [joy/feed-children stubbed-feed-children]
  (joy/occurrences joy/title "Stub" "a" "b" "c"))
;3

;使用clojure.test进行单元测试
(deftest feed-tests
  (with-redefs [joy/feed-children stubbed-feed-children]
    (testing "Children Counting"
      (is (= 1000 (count-feed-entities "DUMMY URL"))))
    (testing "Occurrence Counting"
      (is (= 0 (joy/occurrences
                 joy/title
                 "ZOMG"
                 "DUMMY URL"))))))
;运行测试
(run-tests)
;Testing clojoy.ch17.test
;
;FAIL in (feed-tests) (test.clj:4)
;Children Counting
;expected: (= 1000 (count-feed-entities "DUMMY URL"))
;  actual: (not (= 1000 1))
;
;Ran 1 tests containing 2 assertions.
;1 failures, 0 errors.
;=> {:test 1, :pass 1, :fail 1, :error 0, :type :summary}

;使用前置和后置条件来实现契约式程序设计
(def sqr
  (partial
    (contract
      sqr-contract
      [n]
      (:require (number? n))
      (:ensure (pos? %)))
    #(* % %)))
[(sqr 10) (sqr -9)]
;[100 81]
