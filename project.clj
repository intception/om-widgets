(defproject intception-widgets "0.1.3-SNAPSHOT"
  :description "Widgets for OM/React"
  :url "https://github.com/orgs/intception/"

  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-2311"]
                 [org.clojure/core.async "0.1.267.0-0d7780-alpha"]
                 [om "0.7.1"]
                 [com.andrewmcveigh/cljs-time "0.2.1"]
                 [prismatic/dommy "0.1.3"]
                 [com.palletops/thread-expr "1.3.0"]
                 [prismatic/schema "0.2.6"]]


  :plugins [[lein-cljsbuild "1.0.4-SNAPSHOT"]]
  :source-paths ["src"]
  :cljsbuild {
    :builds [{:id "intception-widgets"
              :source-paths ["src"]
              :compiler {
                :output-to "intception_widgets.js"
                :output-dir "out"
                :optimizations :none
                :source-map true}}]})
