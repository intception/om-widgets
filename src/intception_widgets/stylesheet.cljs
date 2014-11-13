(ns intception-widgets.stylesheet)
(defn- add-style-string!
  [str]
  (let [node (.createElement js/document "style")]
    (set! (.-innerHTML node) str)
    (.appendChild js/document.body node)))

(add-style-string! "")
