(defproject reagent-tops "0.1.0-SNAPSHOT"
  :description "FIXME: write this!"
  :url "http://example.com/FIXME"

  :dependencies [[org.clojure/clojure "1.6.0"]
                 [ring/ring "1.3.0"]
                 [compojure "1.1.8"]
                 [fogus/ring-edn "0.2.0"]]

  :plugins [[lein-cljsbuild "1.0.4-SNAPSHOT"]
            [com.cemerick/clojurescript.test "0.3.1"]]

  :source-paths ["src/clj"]

  :profiles {:dev {:dependencies [[org.clojure/clojurescript "0.0-2311"]
                                  [com.cemerick/clojurescript.test "0.3.1"]
                                  [com.cemerick/double-check "0.5.7"]
                                  [whoops/reagent "0.4.3"]
                                  [prismatic/dommy "0.1.3"]]}}

  :cljsbuild
  {:builds
   [{:id "dev"
     :source-paths ["src/cljs"]
     :compiler {:output-to "resources/public/js/main.js"
                :output-dir "resources/public/js/out"
                :optimizations :none
                :source-map true}}
    {:id "test"
     :source-paths ["src/cljs" "test/cljs"]
     :notify-command ["phantomjs" :cljs.test/runner
                      "test/vendor/es5-shim.js"
                      "test/vendor/es5-sham.js"
                      "test/vendor/console-polyfill.js"
                      "window.literal_js_was_evaluated=true"
                      "target/cljs/testable.js"]
     :compiler {:output-to "target/cljs/testable.js"
                :preamble ["public/js/react-0.11.1.js"]
                :optimizations :whitespace
                :pretty-print true}}]

   :test-commands {"unit" ["phantomjs" :runner
                           "test/vendor/es5-shim.js"
                           "test/vendor/es5-sham.js"
                           "test/vendor/console-polyfill.js"
                           "window.literal_js_was_evaluated=true"
                           "target/cljs/testable.js"]}})
