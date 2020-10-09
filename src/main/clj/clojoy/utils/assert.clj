(ns clojoy.utils.assert)

(defmacro assert-error
  [^Class class & body]
  `(try ~@body
        (catch Throwable ~'e
          (do
            (assert (= ~class (class ~'e)))
            (println "form throws a" (str (.getName ~class) "(" (.getMessage ~'e) ")"))))))
