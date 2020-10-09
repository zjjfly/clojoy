(ns clojoy.experiment.simple-assembler)
(:refer-clojure :rename {inc increase
                         dec decrease})

(declare ^:dynamic heap)

(defmacro mov
  [x y]
  (if (symbol? y)
    `(do
       (assoc! ~'heap ~(keyword x) (get ~'heap ~(keyword y)))
       1)
    `(do
       (assoc! ~'heap ~(keyword x) ~y)
       1)))

(defmacro inc
  [x]
  `(mov ~x (increase (get ~'heap ~(keyword x)))))

(defmacro dec
  [x]
  `(mov ~x (decrease (get ~'heap ~(keyword x)))))

(defmacro jnz
  [x y]
  (if (symbol? x)
    (if (symbol? y)
      `(if (= 0 (get ~'heap ~(keyword x)))
         1
         (get ~'heap ~(keyword y)))
      `(if (= 0 (get ~'heap ~(keyword x)))
         1
         ~y))
    (if (symbol? y)
      `(if (= 0 ~x)
         1
         (get ~'heap ~(keyword y)))
      `(if (= 0 ~x)
         1
         ~y))))

(defn simple-assembler [assembly-code]
  (binding [heap (transient {})]
    (do
      (loop [ip 0]
        (if-let [asm (nth assembly-code ip nil)]
          (-> (eval (read-string (str "(" asm ")")))
              (+ ip)
              long
              (recur))
          heap)))))

(let [code "mov c 12
mov b 0
mov a 200
dec a
inc b
jnz a -2
dec c
mov a b
jnz c -5
jnz 0 1
mov c a"]
  (map (simple-assembler (clojure.string/split-lines code)) [:a :b :c]))

(let [code "mov a 5
inc a
dec a
dec a
jnz a -1
inc a"]
  (= (:a (simple-assembler (clojure.string/split-lines code)))
     1))

(binding [heap (transient {})]
  (mov a 3)
  (mov b 1)
  (dec a)
  (jnz a -1)
  ;(:a heap)
  ;(macroexpand '(jnz a -1))
)
