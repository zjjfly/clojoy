(ns clojoy.ch3.graphics
  (:import (java.awt Frame Graphics Dimension Color)
           (java.lang.reflect Method))
  (:require [clojure.repl :as repl]))

;这里的代码都是在repl中运行的,来显示repl带给我们的便利

;查找clojure文档
(repl/find-doc "xor")
;-------------------------
;clojure.core/bit-xor
;([x y] [x y & more])
;  Bitwise exclusive or

(defn xors [max-x max-y]
  (for [x (range max-x) y (range max-y)]
    [x y (rem (bit-xor x y) 256)]))

(defonce ^Frame frame (Frame.))

;frame默认是不可见的,所以需要查找可以使frame可见的方法
;使用for循环来查找,:let类似于let,进行变量的绑定,:when是过滤表达式,表达式返回true的元素才会保留
(for [meth (.getMethods Frame)
      :let [name (.getName ^Method meth)]
      :when (re-find #"Vis" name)]
  name)
;("setVisible" "isVisible")

;很明显使用setVisible
(.setVisible frame true)

;为窗口设置大小
(.setSize frame (Dimension. 500 500))

;获取窗口的graphics context
(def ^Graphics gfx (.getGraphics frame))

;画一个正方形
(.fillRect gfx 400 400 100 100)

;改变画笔颜色
(.setColor gfx (Color. 255 128 0))
(.fillRect gfx 0 0 75 50)

;使用xors来动态的为这个窗口涂上颜色
(doseq [[x y ^int xor] (xors 500 500)]
  (doto gfx
    (.setColor (Color. xor xor xor))
    (.fillRect x y 1 1)))

;清空窗口中画的图像
(defn clear [^Graphics g x y width height] (.clearRect g x y width height))

(clear gfx 0 0 500 500)

;把获取某个位置的像素颜色的函数改成高阶函数,可以更灵活的修改算法
(defn f-values [f xs ys]
  (for [x (range xs) y (range ys)]
    [x y (rem (f x y) 256)]))

;把画图像的代码封装到一个函数中
(defn draw-values [f xs ys]
  (clear gfx 0 0 xs ys)
  (.setSize frame (Dimension. xs ys))
  (doseq [[x y ^int v] (f-values f xs ys)]
    (.setColor gfx (Color. v v v))
    (.fillRect gfx x y 1 1)))

(draw-values bit-and 256 256)
(draw-values + 256 256)
(draw-values * 256 256)
