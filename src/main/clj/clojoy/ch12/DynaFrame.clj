(ns clojoy.ch12.DynaFrame
  (:import (javax.swing JFrame JPanel JLabel)
           (java.awt BorderLayout))
  (:require [clojoy.utils.assert :as a])
  (:gen-class
   :name clojoy.ch12.DynaFrame
   :extends javax.swing.JFrame
   :implements [clojure.lang.IMeta]
   :prefix "df-"
   :state state
   :init init
   :constructors {[String] [String]
                  [] [String]}
   :methods [[display [java.awt.Container] void]
             ^{:static true} [version [] String]]))
;编译ns为多个class文件
(compile 'clojoy.ch12.DynaFrame)

;如果直接实例化DynaFrame会报错,因为有些方法还没有定义
(a/assert-error UnsupportedOperationException
                (clojoy.ch12.DynaFrame. "First"))
;form throws a java.lang.UnsupportedOperationException(clojoy.ch12.DynaFrame/df-init not defined).

;定义init方法
(in-ns 'clojoy.ch12.DynaFrame)
(defn df-init [title]
  [[title] (atom {:title title})])

;再次尝试实例化,但因为这个类实现了IMeta接口,但没有定义meta方法,所以还是会报错
;(clojoy.ch12.DynaFrame. "2nd")
;Error printing return value (UnsupportedOperationException) at clojoy.ch12.DynaFrame/meta (REPL:-1).
;meta (clojoy.ch12.DynaFrame/df-meta not defined?)

;定义meta和version方法
(defn df-meta [this] @(.state this))
(defn version [] "1.0")

(meta (clojoy.ch12.DynaFrame. "3rd"))
;{:title "3rd"}
(clojoy.ch12.DynaFrame/version)
;1.0

;定义display方法
(defn df-display [^JFrame this ^JPanel pane]
  (doto this
    (-> .getContentPane .removeAll)
    (.setContentPane (doto (JPanel.)
                       (.add pane BorderLayout/CENTER)))
    (.pack)
    (.setVisible true)))

(def gui (clojoy.ch12.DynaFrame. "4th"))

(.display gui (doto (JPanel.)
                (.add (JLabel. "Charlemagne and Pippin"))))

(.display gui (doto (JPanel.)
                (.add (JLabel. "Mater samper certa est."))))
