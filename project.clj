(defproject org.clojars.intception/om-widgets "0.3.8"
  :description "Widgets for OM/React"
  :url "https://github.com/orgs/intception/"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.122" :scope "provided"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [org.omcljs/om "0.8.8" :scope "provided"]
                 [org.clojars.intception/thread-expr "1.4.0"]
                 [com.andrewmcveigh/cljs-time "0.3.13"]
                 [prismatic/dommy "1.0.0"]
                 [net.unit8/tower-cljs "0.1.0"] ;; translations
                 [sablono "0.4.0"]
                 [prismatic/schema "0.4.0" :exclusions [org.clojure/clojurescript]]]

  :plugins [[lein-cljsbuild "1.1.2"]
            [lein-figwheel "0.3.9" :exclusions [org.clojure/clojure]]]

  :source-paths ["src"]
  :test-paths ["test"]

  ; Enable the lein hooks for: compile, test, and jar.
  :hooks [leiningen.cljsbuild]

  :figwheel {:nrepl-port 7889
             :server-port 3450}

  :cljsbuild {:builds [{:id "om-widgets"
                        :figwheel false
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
              :test-commands {"unit-tests" ["phantomjs"
                                            "runners/phantomjs.js"
                                            "resources/private/html/unit-test.html"]}})
