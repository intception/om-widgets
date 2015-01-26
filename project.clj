(defproject org.clojars.intception/intception-widgets "0.1.14"
  :description "Widgets for OM/React"
  :url "https://github.com/orgs/intception/"

  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-2371" :scope "provided"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [om "0.7.1"]
                 [com.palletops/thread-expr "1.3.0"]
                 [com.andrewmcveigh/cljs-time "0.2.1"]
                 [prismatic/dommy "0.1.3"]
                 [net.unit8/tower-cljs "0.1.0"] ;; translations
                 [sablono "0.2.22"]
                 [shodan "0.4.1"]
                 [com.andrewmcveigh/cljs-time "0.2.1"]
                 [prismatic/schema "0.3.3"]]

  :plugins [[lein-cljsbuild "1.0.4-SNAPSHOT"]]
  :source-paths ["src"]
  :cljsbuild {:builds [{:id "intception-widgets"
                        :source-paths ["src"]
                        :compiler {:output-to "intception_widgets.js"
                                   :output-dir "out/intception_widgets"
                                   :optimizations :none
                                   :source-map true}}
                       {:id "basic"
                        :source-paths ["src" "examples/basic/src"]
                        :compiler {:output-to "examples/basic/main.js"
                                   :output-dir "examples/basic/out"
                                   :source-map true
                                   :optimizations :none}}]})
