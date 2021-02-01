(ns om-widgets.utils
  (:require [om.core :as om :include-macros true]
            [cljs.reader :as reader]
            [goog.string :as gstring]
            [goog.string.format]
            [cljs-time.core :as time]
            [cljs-time.format :as timef]))


(defn ->seq
  "Ensures key is a sequence suitable for using with get-in"
  [path-or-key]
  (if (sequential? path-or-key)
    path-or-key
    [path-or-key]))

(defn make-childs
  "Build all alternative for sablono"
  [tag & childs]
  (vec (apply concat
              (if (vector? tag) tag [tag])
              childs)))

(defn format
  [fmt & args]
  (apply gstring/format fmt args))

(defn atom? [x]
  (and (satisfies? cljs.core/IDeref x)
       (not (satisfies? om/ICursor x))))

(defn om-update! [tgt key value]
  (if (atom? tgt)
    (if-not (nil? key)
      (if (fn? value)
        (swap! tgt value)
        (swap! tgt assoc key value))
      (reset! tgt value))
    (if (= nil key)
      (if (fn? value)
        (om/transact! tgt value)
        (om/update! tgt value))
      (if (satisfies? om/ISetState tgt)
        (if (fn? value)
          (om/update-state! tgt key value)
          (om/set-state! tgt key value))
        (if (fn? value)
          (if-not (= nil key)
            (om/transact! tgt key value)
            (om/transact! tgt value))
          (om/update! tgt key value))))))

(defn om-get [tgt key]
  (if (atom? tgt)
    (if-not (nil? key)
      (get-in @tgt (if (sequential? key) key [key]))
      @tgt)
    (if (satisfies? om/IGetState tgt)
      (om/get-state tgt key)
      (if-not (= nil key)
        (let [ks (if (sequential? key) key [key])]
          (get-in tgt ks))
        tgt))))

(defn browser-support-input-type?
  "More info: http://diveintohtml5.info/detect.html#input-types"
  [input-type]
  (let [e (.createElement js/document "input")]
    (.setAttribute e "type" input-type)
    (not= "text" (.-type e))))

(defn el-data
  " Example:

  <div data-example=':something'></div>

  (el-data el :example)
  :something
  "
  [el key]
  (reader/read-string (.getAttribute el (str "data-" (name key)))))

(defn- get-hr-month
  "Returns a human readable month from a cljs-time date."
  [date]
  (timef/unparse (timef/formatter "MMMM") date))

(defn- get-date
  "Returns a human readable date from a cljs-time date."
  [date]
  (timef/unparse (timef/formatter "yyyy/MM/dd") (time/date-time date)))

(defn zero-pad
  "Given a 0 string will return a new string with 0
  paddings to the begining of the string.

  Example:
    (zero-pad '01' 1)
    '001'
  "
  [s len]
  (str (apply str (repeat (- len (count (str s)))  "0")) s))

(defn get-utc-formatted-date
  "Example:

  (get-date #inst '1990-06-09')
  '09/06/1990'
  "
  [date]
  (str (zero-pad (.getUTCDate date) 2) "/"
       (zero-pad (inc (.getUTCMonth date)) 2) "/"
       (.getUTCFullYear date)))

(defn glyph
  [icon]
  (str "glyphicon glyphicon-" (name icon)))

(defn exclude-item-at-pos
  "Given a vector and position number, will return a new
  vector with the element excluded.

  Note: Indext start at 0

  Example:
    (exclude-item-at-pos [1 2 3] 1))
    [1 3]
  "
  [v p]
  (let [vec-len (count v)
        has-items? (pos? vec-len)
        in-bounds? (< p vec-len)]
    (if (and has-items? in-bounds?)
      (vec (concat (subvec v 0 p) (subvec v (inc p))))
      v)))

(defn get-window-boundaries!
  "Get js/window bounderies {:width, :height}
  Note: fn with side-effects"
  []
  (cond
    (not= nil (type (.-innerWidth js/window)))
    {:width (.-innerWidth js/window)
     :height (.-innerHeight js/window)}

    (and (not= nil (type (.-documentElement js/document)))
         (.-clientWidth js/document.documentElement)
         (not= 0 (.-documentElement.clientWidth js/document)))
    {:width (.-clientWidth js/document.documentElement)
     :height (.-clientHeight js/document.documentElement)}

    :else
    {:width (.-clientWidth (aget (.getElementsByTagName js/document "body") 0))
     :height (.-clientHeight (aget (.getElementsByTagName js/document "body") 0))}))

(defn re-quote [s]
  (let [special (set ".?*+^$[]\\(){}|")
        escfn #(if (special %) (str \\ %) %)]
    (apply str (map escfn s))))
