(ns clojoy.ch8.macro2
  (:require [clojure.xml :as xml]))


;使用宏改变form
;以实现一个描述人和怪兽的战斗情况的宏为例,预期的效果是下面这样的:
;(domain man-vs-monster
;  (grouping people
;    (Human "A stock human")
;    (Man (isa Human)
;    "A man, baby" [name] [has-beard?]))
;  (grouping monsters
;    (Chupacabra
;"A fierce, yet elusive creature" [eats-goats?])))
;这段代码可以描述下面这样的关系:
;Man versus monster
;  People
;    • Men (humans)
;      - Name
;      - Have beards?
;  Monsters
;    • Chupacabra
;      - Eats goats?
;在底层,使用下面这样的map来存储数据:
;{:tag <node form>,
; :attrs {},
; :content [<nodes>]
;}
;这个结构不算好看,但比较整齐和简单,并且可以被clojure.xml库识别
(defmacro domain
  [name & body]
  `{:tag     :domain,
    :attrs   {:name (str '~name)},
    :content [~@body]})
(declare handle-things)

(defmacro grouping
  [name & body]
  `{:tag     :grouping,
    :attrs   {:name (str '~name)},
    :content [~@(handle-things body)]})

(declare grok-attrs grok-props)

(defn handle-things
  [things]
  (for [t things]
    {:tag     :thing,
     :attrs   (grok-attrs (take-while (comp not vector?) t))
     :content (if-let [c (grok-props (drop-while (comp not vector?) t))]
                [c] [])}))

(defn grok-attrs [attrs]
  (into {:name (str (first attrs))}
        (for [a (rest attrs)]
          (cond
            (list? a) [:isa (str (second a))]
            (string? a) [:comment a]))))

(defn grok-props
  [props]
  (when props
    {:tag :properties, :attrs nil,
     :content
          (apply vector (for [p props]
                          {:tag     :property,
                           :attrs   {:name (str (first p))},
                           :content nil}))}))

(def d
  (domain man-vs-monster
          (grouping people
                    (Human "A stock human")
                    (Man (isa Human)
                         "A man, baby" [name] [has-beard?]))
          (grouping monsters
                    (Chupacabra
                      "A fierce, yet elusive creature" [eats-goats?]))))
(:tag d)
;:domain
(:tag (first (:content d)))
;:grouping

(xml/emit d)
;<?xml version='1.0' encoding='UTF-8'?>
;<domain name='man-vs-monster'>
; <grouping name='people'>
;   <thing name='Human' comment='A stock human'>
;     <properties></properties>
;   </thing>
;   <thing name='Man' isa='Human' comment='A man, baby'>
;     <properties>
;       <property name='name'/>
;       <property name='has-beard?'/>
;     </properties>
;   </thing>
; </grouping>
; <grouping name='monsters'>
;   <thing name='Chupacabra' comment='A fierce, yet elusive creature'>
;     <properties>
;       <property name='eats-goats?'/>
;     </properties>
;   </thing>
; </grouping>
;</domain>

;上面的这些函数和宏其实可以写在一个大的宏中,但分成多个宏和函数更便于修改其组成部分
