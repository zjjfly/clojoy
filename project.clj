(defproject clojoy "0.1.0-SNAPSHOT"
  :description "Code of \"The Joy of Clojure\""
  :url "https://github.com/zjjfly/clojoy"
  :license {:name "Apache 2.0"
            :url  "https://www.apache.org/licenses/LICENSE-2.0"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [junit/junit "4.12"]
                 [org.clojure/core.async "1.2.603"]
                 [criterium "0.4.6"]]
  :main ^:skip-aot clojoy.core
  :plugins [[lein-cljfmt "0.5.7"]
            [lein-junit "1.1.8"]]
  :prep-tasks ["javac" "compile"]
  :target-path "target/%s"
  :global-vars {*warn-on-reflection* true}
  :source-paths ["src/main/clj"]
  :java-source-paths ["src/main/java" "src/test/java"]
  :javac-options ["-target" "1.8" "-source" "1.8" "-encoding" "utf8" "-Xlint:-options"]
  :resource-paths ["src/main/resources"]
  :test-paths ["src/test/clj"]
  :junit ["src/test/java"]
  :profiles {:uberjar {:aot :all}
             :kaocha  {:dependencies [[lambdaisland/kaocha "1.0.700"]
                                      [org.clojure/test.check "1.1.0"]
                                      ]}}
  :aliases {"kaocha" ["with-profile" "+kaocha" "run" "-m" "kaocha.runner"]}
  )
