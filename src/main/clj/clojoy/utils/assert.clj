(ns clojoy.utils.assert
  (:import (clojure.lang ExceptionInfo)))

(defmacro assert-error
  [^Class class & body]
  `(try (do ~@body
            (throw
             (ex-info (str (.getName ~class) " should be thrown,but it did not!")
                      {:just-for-distinguish true})))
        (catch ExceptionInfo ~'e
          (if (:just-for-distinguish (.getData ~'e))
            (throw ~'e)
            (throw (AssertionError. (str (.getName ~class) " should be thrown,not clojure.lang.ExceptionInfo!")))))
        (catch Throwable ~'e
          (do
            (assert (= ~class (class ~'e))
                    (str (.getName ~class) " should be thrown,not " (.getName (class ~'e)) "!"))
            (println "form throws a" (str (.getName ~class) "(" (.getMessage ~'e) ")."))))))
