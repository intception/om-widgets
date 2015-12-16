(ns om-widgets.textinput
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [om-widgets.utils :as utils]
            [cljs-time.format :as time-format]
            [cljs-time.core :as time]
            [cljs-time.local :as time-local]
            [cljs-time.coerce :as timec]))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(def date-local-format "dd/MM/yyyy")
(def date-local-mask "00/00/0000")
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn- date-from-localstring
  [value fmt]
  (let [d (time-format/parse (time-format/formatter fmt) value)]
    (js/Date. d)))

(defn- string-from-date
  [dt fmt]
  (let [dt (timec/from-date dt)]
    (time-format/unparse (time-format/formatter fmt) dt)))


(defn- convert-input
  [input-type value]
  (condp = input-type
    "date" (try
             (string-from-date value date-local-format)
             (catch js/Error e
               ;; assume empty string for unhandled values
               (str value)))
    value))

(defn- convert-output
  [output-type value]
  (condp = output-type
    "date" (try
             (date-from-localstring value date-local-format)
             (catch js/Error e
               value))
    "numeric" (let [f (js/parseFloat value)]
                (if (js/isNaN f) value f))

    value))

(defn replace-item-at-pos
  "Given a vector and position number, will return a new
  vector with the element replaced."
  [v p n]
  (let [vec-len (count v)
        has-items? (pos? vec-len)
        in-bounds? (< p vec-len)]
    (if (and has-items? in-bounds?)
      (vec (concat (subvec v 0 p) [n] (subvec v (inc p))))
      v)))

(defn- erase-selection
  [mask-vector entered-values sel-start sel-end]
  (if (= sel-start sel-end)
    entered-values
    (recur mask-vector
           (if-not (string? (nth mask-vector sel-start))
             (replace-item-at-pos entered-values sel-start \_)
             entered-values)
           (inc sel-start)
           sel-end)))

(defn- next-available-position
  [mask-vector pos]
  (if (and (> (count mask-vector) pos) (string? (nth mask-vector pos)))
    (recur mask-vector (inc pos))
    pos))

(defn- special-key?
  [char-code]
  (contains? #{9	;; tab
               13	;; enter
               16	;; shift
               17	;; ctrl
               18	;; alt
               20	;; caps lock
               27	;; escape
               33	;; page up
               34	;; page down
               35	;; home
               36	;; end
               37	;; left arrow
               38	;; up arrow
               39	;; right arrow
               40	;; down arrow
               45	;; insert
               144} ;; num lock;
             char-code))

(defn- get-selection-start ;; assume modern browser IE9 and up
  [control]
  (.-selectionStart control))

(defn- get-selection-end ;; assume modern browser IE9 and up
  [control]
  (.-selectionEnd control))

(defn- set-caret-pos
  [control pos]
  (.setSelectionRange control pos pos))

(defn- mask-handler-selector
  [target owner state]
  (condp = (:input-format state)
    "numeric" :numeric
    "password" :unmasked
    nil :unmasked
    :mask))

(defn- update-target
  [target owner {:keys [cbtimeout input-format prev-value path onChange private-state] :as state} bInternal]
  (when target
    (let [dom-node (:dom-node @private-state)
          value (convert-output input-format (.-value dom-node))]
      (do
        (swap! private-state assoc :cbtimeout nil :prev-value value)
        (utils/om-update! target path value)
        (when (and onChange (not bInternal))
          (onChange value))))))

(defn- fire-on-change
  [target owner {:keys [cbtimeout typing-timeout prev-value path private-state onChange] :as state}]
  (let [cbtimeout (:cbtimeout @private-state)]
    (when cbtimeout
      (.clearTimeout js/window cbtimeout))
    (swap! private-state assoc :cbtimeout (.setTimeout js/window #(update-target target owner state false)
                                                       (or typing-timeout 500)))))


;; ---------------------------------------------------------------------
;; handle-custom-keys
(defmulti handle-custom-keys! mask-handler-selector)

(defmethod handle-custom-keys! :mask
  [target owner state k]
  (let [private-state (:private-state state)
        dom-node (:dom-node @private-state)
        mask-vector (:mask-vector @private-state)
        entered-values (:entered-values @private-state)
        sel-start (get-selection-start dom-node)
        sel-end (get-selection-end dom-node)]
    (if (contains? #{8 46} k)
      (do
        (if (= sel-start sel-end)
          (condp = k
            ;; backspace
            8 (let [pos (dec sel-start)]
                   (when (>= pos 0)
                     (do
                       (when-not (string? (nth mask-vector pos))
                         (let [new-entered-values (replace-item-at-pos entered-values pos \_)]
                           (swap! private-state assoc :entered-values new-entered-values)
                           (set! (.-value dom-node)  (apply str new-entered-values))))
                       (set-caret-pos dom-node pos))))
            ;; delete
            46 (when (< sel-start (count mask-vector))
                 (do
                   (when-not (string? (nth mask-vector sel-start))
                     (let [new-entered-values (replace-item-at-pos entered-values sel-start \_)]
                       (swap! private-state assoc :entered-values new-entered-values)
                       (set! (.-value dom-node)  (apply str new-entered-values)))))
                 (set-caret-pos dom-node (inc sel-start))))
          ;; Selection
          (let [new-entered-values (erase-selection mask-vector entered-values sel-start sel-end)]
            (swap! private-state assoc :entered-values new-entered-values)
            (set! (.-value dom-node)  (apply str new-entered-values))
            (set-caret-pos dom-node sel-start)))
        false)
      true)))

(defmethod handle-custom-keys! :default
  [target owner state k]
  true)

;; ---------------------------------------------------------------------
;; handlekeydown
(defmulti handlekeydown mask-handler-selector)

(defmethod handlekeydown :mask
  [target owner state e]
  (let [k (.-which e)]
    (if (special-key? k)
      true
      (do
        (fire-on-change target owner state)
        (handle-custom-keys! target owner state k)))))

(defmethod handlekeydown :numeric
  [target owner state e]
  (let [k (.-which e)]
    (if (special-key? k)
      true
      (do
        (fire-on-change target owner state)
        (handle-custom-keys! target owner state k)))))

(defmethod handlekeydown :default
  [target owner state e]
  (fire-on-change target owner state)
  true)

;; ---------------------------------------------------------------------
;; handlekeyup
(defmulti handlekeyup mask-handler-selector)

(defmethod handlekeyup :mask
  [target owner state e]
  (let [k (.-which e)]
    (if (special-key? k)
      true)
    false))

(defmethod handlekeyup :default
  [target owner state e]
  true)


;; ---------------------------------------------------------------------
;; handlekeypress
(defmulti handlekeypress mask-handler-selector)

(defmethod handlekeypress :mask
  [target owner state e]
  (let [private-state (:private-state state)
        dom-node (:dom-node @private-state)
        mask-vector (:mask-vector @private-state)
        entered-values (:entered-values @private-state)
        mi (nth mask-vector (min (get-selection-start dom-node) (dec (count mask-vector))))
        pos (next-available-position mask-vector (get-selection-start dom-node))
        char-code (.-which e)
        new-char (.fromCharCode js/String char-code)
        entered-values (:entered-values @private-state)]
    (when (and pos (> (count mask-vector) pos))
      (let [m (nth mask-vector pos)]
        (if (re-matches m new-char)
          (let [new-entered-values (replace-item-at-pos entered-values pos new-char)]
            (swap! private-state assoc :entered-values new-entered-values)
            (set! (.-value dom-node)  (apply str new-entered-values))
            (set-caret-pos dom-node (inc pos)))
          (if (string? mi)
            (set-caret-pos dom-node pos)
            (set-caret-pos dom-node (inc pos))))))
    false))

(defmethod handlekeypress :numeric
  [target owner state e]
  (let [char-code (.-which e)
        new-char (.fromCharCode js/String char-code)]
    (pos? (count (re-seq #"\d" new-char)))))

(defmethod handlekeypress :default
  [target owner state e]
  true)

;; ---------------------------------------------------------------------
;; Apply Mask
(defmulti applymask! mask-handler-selector)

(defmethod applymask! :mask
  [target owner state value]
  (let [private-state (:private-state state)
        entered-values ((fn [mv cv & s]
                          (let [r (vec s)
                                m (first mv)
                                c (or (first cv) \_)]
                            (if m
                              (if (string? m)
                                (if (= m c)
                                  (recur (next mv) (next cv) (conj r m))
                                  (recur (next (next mv)) (next cv) (conj r m c)))
                                (recur (next mv) (next cv) (conj r (if (re-matches m c) c \_))))
                              r)))
                        (:mask-vector @private-state)
                        (vec (convert-input (:input-format state) value)))
        prev-value (:prev-value @private-state)
        new-value (apply str entered-values)
        dom-node (:dom-node @private-state)]
    (when (and (not= prev-value new-value) dom-node)
      (do
        (swap! private-state assoc :entered-values entered-values
               :prev-value new-value)
        (set! (.-value dom-node)  new-value)))))

(defmethod applymask! :default
  [target owner state value]
  (when-let  [dom-node (:dom-node @(:private-state state))]
    (when-not  (= value (:prev-value @(:private-state state)))
      (set! (.-value dom-node) value))))


;; ---------------------------------------------------------------------
;; handlepaste
(defmulti handlepaste mask-handler-selector)

(defmethod handlepaste :mask
  [target owner state k]
  (.setTimeout js/window (fn []
                           (let [private-state (:private-state state)
                                 dom-node (:dom-node @private-state)]
                             (applymask! target owner state (.-value dom-node))))
               1)
  true)

(defmethod handlepaste :default
  [target owner state e]
  true)


;; ---------------------------------------------------------------------
;; Init Mask
(defmulti initmask! mask-handler-selector)

(defmethod initmask! :mask
  [target owner state]
  (let [private-state (:private-state state)
        input-mask (:input-mask state)
        mask (vec (map #(condp = %
                          \0 #"^[0-9]$"
                          \# #"^[0-9s%.]$"
                          \L #"^[a-zA-Z]$"
                          \A #"^[0-9a-zA-Z]$"
                          \& #"."
                          \\ nil
                          %) input-mask))]
    (swap! private-state assoc :entered-values (map #(when (string? %) %) mask)
           :mask-vector (remove nil? mask))))

(defmethod initmask! :default
  [target owner state])


;; ---------------------------------------------------------------------
;; Components
(defn- create-textinput
  [target owner]
  (reify
    om/IInitState
    (init-state [this]
      {:private-state (atom {})})

    om/IWillMount
    (will-mount [this]
      (initmask! target owner (om/get-state owner)))

    om/IDidMount
    (did-mount [this]
      (let [state (om/get-state owner)
            private-state (:private-state state)]
        (swap! private-state assoc :dom-node (om.core/get-node owner))
        (applymask! target owner state (utils/om-get target (:path state)))))


    om/IRenderState
    (render-state [this state]
      (applymask! target owner state (utils/om-get target (:path state)))
      ((if (not (:multiline state))
         dom/input
         dom/textarea)
       (clj->js {:id (:id state)
                 :name (:id state)
                 :hidden (:hidden state)
                 :className (clojure.string/join " " ["om-widgets-input-text" (:input-class state)])
                 :autoComplete (:auto-complete state)
                 :readOnly (:read-only state)
                 :onKeyDown #(if (false? (handlekeydown target owner state %))
                              (.preventDefault %)
                              nil)
                 :onKeyUp #(if (false? (handlekeyup target owner state %))
                            (.preventDefault %)
                            nil)
                 :onKeyPress #(do
                                (when (= "Enter" (.-key %))
                                  (do
                                    (when (and (:flush-on-enter state)
                                               (not (:multiline state)))
                                      (update-target target owner state true))
                                    (when (:onEnter state)
                                      ((:onEnter state) %))))

                                (when (:onKeyPress state)
                                  ((:onKeyPress state) %))
                                (if (false?  (handlekeypress target owner state %))
                                  (.preventDefault %)
                                  nil))
                 :autoFocus (:autofocus state)
                 :tabIndex (:tabIndex state)
                 :onBlur (fn [e]
                           (update-target target owner state true)
                           (when (:onBlur state)
                             ((:onBlur state)))
                           nil)
                 :onPaste #(if (false? (handlepaste target owner state %))
                            (.preventDefault %)
                            nil)
                 :placeholder (:placeholder state)
                 :disabled (:disabled state)
                 :typing-timeout (:typing-timeout state)
                 :type (if (= (:input-format state) "password")
                         "password"
                         "text")
                 :style {:textAlign (:align state)}})))))


(defn textinput
  [target path {:keys [input-class input-format multiline onBlur tabIndex autofocus
                       placeholder id decimals align onChange auto-complete read-only disabled onKeyPress
                       typing-timeout flush-on-enter onEnter hidden] :as opts
                :or {input-class ""}}]
  (om/build create-textinput target
            {:state {:path path
                     :input-class input-class
                     :input-format input-format
                     :multiline multiline
                     :tabIndex tabIndex
                     :autofocus autofocus
                     :placeholder placeholder
                     :id id
                     :hidden hidden
                     :disabled disabled
                     :typing-timeout typing-timeout
                     :flush-on-enter flush-on-enter
                     :read-only read-only
                     :input-mask (cond
                                   (= input-format "numeric") "numeric"
                                   (= input-format "integer") "numeric"
                                   (= input-format "currency") "numeric"
                                   (= input-format "date") date-local-mask
                                   :else input-format)
                     :decimals (cond
                                 (= input-format "currency") (if (not decimals) 2 decimals)
                                 (= input-format "numeric") (if (not decimals) 2 decimals)
                                 :else 0)
                     :currency (if (= input-format "currency") true false)
                     :align (cond (= input-format "numeric") "right"
                                  (= input-format "integer") "right"
                                  (= input-format "currency") "right"
                                  :else align)
                     :onChange onChange
                     :onBlur onBlur
                     :onKeyPress onKeyPress
                     :onEnter onEnter
                     :auto-complete auto-complete}}))
