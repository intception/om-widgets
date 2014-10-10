(ns intception-widgets.forms
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [pallet.thread-expr :refer [when-> when->>]]))

(defmacro class-from-opts
  "Creates"
  [opts]
  `(->> ["form-group"]
        (when->> (:warning-message ~opts)
                 (cons "has-warning"))
        (when->> (:error-message ~opts)
                 (cons "has-error"))
        (when->> (:required ~opts)
                 (cons "required"))
        (cons (:extra-class ~opts))
        (clojure.string/join " ")))

(defmacro field-component
  "Adds an optional error or warning message to the field, the body
   received is always wrapped inside a <div>."
  [opts & body]
  `(dom/div (cljs.core/clj->js {:className (class-from-opts ~opts)})
            ~@(conj (vec body)
                    `(when (or (:error-message ~opts)
                               (:warning-message opts))
                       (dom/span (cljs.core/clj->js {:className "help-block"})
                                 (str (:error-message ~opts)
                                      (:warning-message ~opts)))))))

(defmacro form
  "Macro to create a form, binds errors and entity to be accesible
   inside the form by every rendered component."
  [entity owner {:keys [title subtitle errors validation]} & body]
  `(binding [intception-widgets.forms/*errors* ~errors
             intception-widgets.forms/*entity* ~entity
             intception-widgets.forms/*owner* ~owner
             intception-widgets.forms/*validation-rules* ~validation]
     (dom/div (cljs.core/clj->js {:className "container container-fluid form"})

              (when (om/get-state ~owner :saving)
                (dom/div (cljs.core/clj->js {:className "overlay"})))

              (when (om/get-state ~owner :saving)
                (dom/div (cljs.core/clj->js {:className "wait-logo"})))

              ;;form header section
              (when ~title
                (dom/div (cljs.core/clj->js {:className "panel-heading"})
                         (dom/h3 (cljs.core/clj->js {:className "panel-title"})
                                 ~title)
                         dom/small nil ~subtitle))
              ;;form body section
              (dom/div (cljs.core/clj->js {:className "panel-body"})
                       (dom/form (cljs.core/clj->js {:sclassName "form-horizontal right"
                                                     :role "form"})
                                 ~@body))
              (when (om/get-state ~owner :general-error)
                (dom/p (cljs.core/clj->js {:className "bg-danger"})
                       (om/get-state ~owner :general-error))))))

(defmacro section
  "Macro to create a header field section (also known as fieldset)"
  [title & body]
  `(dom/div (cljs.core/clj->js {:className "form-group"})
            (dom/h3 nil ~title)))

(defmacro helper-input
  "If a bootstrap helper plugin is specified input field is rendered inside a helper div"
  [helper body]
  `(if ~helper
     (dom/div (cljs.core/clj->js {:className "input-group"})
              (dom/div (cljs.core/clj->js {:className "input-group-addon"}) ~helper)
              ~body)
     ~body))


(defmulti create-field (fn [_ opts] (:type opts)))

(defmethod create-field :text
  [key {:keys [helper placeholder input-format
               validate-on-blur disabled read-only multiline]}]
  `(helper-input ~helper
                 (intception-widgets.textinput/textinput
                  intception-widgets.forms/*entity* ~key
                  :id ~key
                  :placeholder ~placeholder
                  :disabled ~disabled
                  :multiline ~multiline
                  :read-only ~read-only
                  :input-class "form-control"
                  :input-format ~input-format
                  ;;a partial trick is done here in order to evaluate
                  ;;the binded atributes when the function is defined
                  ;;and not when it's called
                  :onBlur (when (and (not= false ~validate-on-blur)
                                     intception-widgets.forms/*validation-rules*)
                            (partial intception-widgets.forms/validate-key
                                     intception-widgets.forms/*owner*
                                     ~key
                                     intception-widgets.forms/*entity*
                                     intception-widgets.forms/*validation-rules*)))))


(defmethod create-field :combo
  [key {:keys [options read-only disabled on-blur on-change]}]
  `(intception-widgets.combobox/combobox
    intception-widgets.forms/*entity* ~key
    :class-name "form-control"
     :id ~key
     :disabled ~disabled
     :read-only ~read-only
     :options ~options
     :onBlur ~on-blur
     :onChange ~on-change))

(defmethod create-field :radio
  [key {:keys [options read-only disabled]}]
  `(intception-widgets.radiobutton/radiobutton-group
    intception-widgets.forms/*entity* ~key
     :class-name "form-control"
     :id ~key
     :disabled ~disabled
     :options ~options ))

(defmethod create-field :check
  [key {:keys [text caption disabled]}]
  `(intception-widgets.checkbox/checkbox
    intception-widgets.forms/*entity* ~key
     :id ~key
     :disabled ~disabled
     :label ~caption))

;;
;; (defmethod create-field :listview
;;   [key {:keys [input-format is-valid? disabled]}]
;;   `(widgets.editable-list-view/editable-list-view
;;     intception-widgets.forms/*entity* ~key
;;     :input-format ~input-format
;;     :id ~key
;;     :disabled ~disabled
;;     :field-validation-fn ~is-valid?))
;;
;; (defmethod create-field :autosuggest
;;   [key opts]
;;   `(widgets.autosuggest/autosuggest
;;     intception-widgets.forms/*entity* ~key
;;     ~opts))

(defmethod create-field :default
  [key opts]
  `(dom/div nil
            (str "Control type [" (:type ~opts) "] unsupported")))


(defmacro field
  "Creates a form field with label, input type is created dynamically selected
   from type and extensible using create-field multimethod."
  [key {:keys [label required extra-class] :as options}]
  `(field-component {:error-message (get intception-widgets.forms/*errors* ~key)
                     :required ~required
                     :extra-class ~extra-class}
                    (when ~label
                        (dom/label (cljs.core/clj->js {:className "control-label"
                                                       :for ~key})
                                   ~label))
                    ~(create-field key options)))


(defmacro button-class
  [location style]
  `(->> ["btn"]
        (cons (str "btn-" (name (or ~style :primary))))
        (when->> (= :right ~location)
                 (cons "pull-right"))
        (clojure.string/join " ")))

(defmacro button
  [action {:keys [text location on-click on-valid icon style disabled]}]
  `(dom/button
    (cljs.core/clj->js {:className (button-class ~location ~style)
                        :type "button"
                        :id ~(name action)
                        :disabled ~disabled
                        ;;on-valid takes preference over on-click
                        ;;same partial trick as above to evaluate
                        ;;bindings on function definition
                        :onClick (partial intception-widgets.forms/button-clicked
                                          intception-widgets.forms/*owner*
                                          intception-widgets.forms/*entity*
                                          (when ~on-valid
                                            intception-widgets.forms/*validation-rules*)
                                          ~(or on-valid
                                               on-click))})
    (dom/span (cljs.core/clj->js {:className (str ""
                                                  (name ~icon))}))
    (str "  " ~text)))

(defmacro with-owner
  [owner & body]
  `(binding [intception-widgets.forms/*owner* ~owner]
     ~@body))

(defmacro row
  [& cols]
  `(dom/div (cljs.core/clj->js {:className "row"})
    ~@cols))

(defmacro column
  [width & fields]
  `(dom/div nil ~@ (for[f fields]
                (reverse (into '() (assoc-in (vec f)
                                             [2 :extra-class]
                                             (str "col-" width)))))))

