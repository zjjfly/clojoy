(ns clojoy.ch17.design-pattern)

;clojure实现部分的设计模式

;观察者模式，利用clojure自带的add-watch
(defmacro def-formula
  [nm bindings & formula]
  `(let ~bindings
     (let [formula# (agent ~@formula)
           update-fn# (fn [key# ref# o# n#]
                        (send formula# (fn [_#] ~@formula)))]
       (doseq [r# ~(vec (map bindings
                             (range 0 (count bindings) 2)))]
         (add-watch r# :update-formula update-fn#)
         (def ~nm formula#)))))

(def h (ref 25))
(def ab (ref 100))
(def-formula
  avg
  [at-bats ab, hits h]
  (float (/ @hits @at-bats)))
@avg
;0.25

(dosync (ref-set h 33))
@avg
;0.33

;策略模式，可以通过把要用的函数传入高阶函数来实现。
;使用clojure的多重方法比传统的策略模式更强大

;访问者模式，在协议，多重方法，proxy，reify等特性中可以看到其影子

;抽象工程模式
(def config
  '{:systems {:pump {:type :feeder, :descr "Feeder system"}
              :sim1 {:type :sim, :fidelity :low}
              :sim2 {:type :sim, :fidelity :high, :threads 2}}})

(defn describe-system [name cfg]
  [(:type cfg) (:fidelity cfg)])

(defmulti construct describe-system)

(defmethod construct :default [name cfg]
  {:name name
   :type (:type cfg)})

(defn construct-subsystems [sys-map]
  (for [[name cfg] sys-map]
    (construct name cfg)))

(construct-subsystems (:systems config))
;({:name :pump, :type :feeder} {:name :sim1, :type :sim} {:name :sim2, :type :sim})

(defmethod construct [:feeder nil]
  [_ cfg]
  (:descr cfg))
(construct-subsystems (:systems config))
;("Feeder system" {:name :sim1, :type :sim} {:name :sim2, :type :sim})

(defrecord LowFiSim [name])
(defrecord HiFiSim [name threads])

(defmethod construct [:sim :low]
  [name cfg]
  (->LowFiSim name))

(defmethod construct [:sim :high]
  [name cfg]
  (->HiFiSim name (:threads cfg)))

(construct-subsystems (:systems config))
;; ("Feeder system"
;;  {:name :sim1}
;;  {:name :sim2, :threads 2})
