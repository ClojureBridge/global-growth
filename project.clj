(defproject cheap-life "0.1.0-SNAPSHOT"
  :description "Demonstrates the use of the World Bank API"
  :url "https://github.com/clojurebridge/cheap-life"
  :license {:name "Creative Commons Attribution License"
            :url "http://creativecommons.org/licenses/by/3.0/"}
  :dependencies [[org.clojure/clojure "1.5.1"]
		 [clj-http "0.7.7"]
                 [cheshire "5.2.0"]]
  :main ^:skip-aot cheap-life.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
