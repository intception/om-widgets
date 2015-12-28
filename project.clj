(defproject org.clojars.intception/om-widgets "0.2.23"
  :description "Widgets for OM/React"
  :url "https://github.com/orgs/intception/"

  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.122" :scope "provided"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [org.omcljs/om "0.8.8" :scope "provided"]
                 [com.palletops/thread-expr "1.3.0"]
                 [com.andrewmcveigh/cljs-time "0.3.13"]
                 [prismatic/dommy "1.0.0"]
                 [net.unit8/tower-cljs "0.1.0"] ;; translations
                 [sablono "0.4.0"]
                 [com.cemerick/clojurescript.test "0.3.3"]
                 [prismatic/schema "0.4.0" :exclusions [org.clojure/clojurescript]]]

  :plugins [[lein-cljsbuild "1.0.5"]
            [lein-figwheel "0.3.9" :exclusions [org.clojure/clojure]]
            [com.cemerick/clojurescript.test "0.3.3"]]

  :source-paths ["src"]
  :test-paths ["test"]

  :figwheel {:nrepl-port 7889
             :server-port 3450}

  :cljsbuild {:builds [{:id "om-widgets"
                        :source-paths ["src"]
                        :compiler {:output-to "target/om_widgets.js"
                                   :output-dir "target"
                                   :optimizations :none
                                   :source-map true}}
                       {:id "test"
                        :source-paths ["src" "test"]
                        :compiler {:pretty-print true
                                   :output-dir "target/test"
                                   :output-to "target/test/unit-test.js"
                                   :preamble ["react/react.js"]
                                   :externs ["react/externs/react.js"]
                                   :optimizations :whitespace}}
                       {:id "basic"
                        :figwheel {:on-jsload "examples.basic.core/examples"}
                        :source-paths ["src" "src/examples"]
                        :compiler {:output-to "resources/public/examples/basic/main.js"
                                   :output-dir "resources/public/examples/basic/out"
                                   :optimizations :none
                                   :source-map true}}]
              :test-commands {"unit-tests" ["phantomjs" :runner
                                            "test/vendor/es5-shim.js"
                                            "test/vendor/es5-sham.js"
                                            "test/vendor/console-polyfill.js"
                                            "target/test/unit-test.js"]}})
