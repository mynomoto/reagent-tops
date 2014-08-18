(defproject reagent-tops "0.1.0-SNAPSHOT"
  :description "FIXME: write this!"
  :url "http://example.com/FIXME"

  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-2311"]
                 [ring/ring "1.3.0"]
                 [compojure "1.1.8"]
                 [fogus/ring-edn "0.2.0"]
                 [whoops/reagent "0.4.3"]]

  :plugins [[lein-cljsbuild "1.0.4-SNAPSHOT"]]

  :source-paths ["src/clj"]

  :cljsbuild {
    :builds [{:id "reagent-tops"
              :source-paths ["src/cljs"]
              :compiler {
                :output-to "resources/public/js/main.js"
                :output-dir "resources/public/js/out"
                :optimizations :none
                :source-map true}}]})
