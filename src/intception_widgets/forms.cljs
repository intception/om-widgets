(ns intception-widgets.forms
  (:require [schema.core :as schema]
            [intception-widgets.textinput :refer [textinput]]
            [intception-widgets.combobox :refer [combobox]]
            [intception-widgets.checkbox :refer [checkbox]]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]))

(def ^:dynamic *entity*)
(def ^:dynamic *errors*)
(def ^:dynamic *validation-rules*)
(def ^:dynamic *owner*)

(defn validate
  [entity rules messages]
  (try
    (do (schema/validate rules entity)
        nil)
    (catch js/Error e
      (select-keys messages (keys (:error (ex-data e)))))))

(defn validate-key
  [owner key entity rules]
  (om/set-state! owner [:errors key] (get (validate (if (:pre-validation-transformation rules)
                                                        ((:pre-validation-transformation rules) @entity)
                                                         @entity)
                                                    (:rules rules)
                                                    (:messages rules))
                                          key)))

(defn button-clicked
  [owner entity rules continuation]
  (println (:pre-validation-transformation rules))
  (if rules
    (if-let [errors (validate (if (:pre-validation-transformation rules)
                                  ((:pre-validation-transformation rules) @entity)
                                                         @entity)
                              (:rules rules)
                              (:messages rules))]
      (do
        (om/set-state! owner :general-error (or (:validate-failed-msg rules)
                                                "El formulario contiene errores de validación, verifique los campos y vuelva a intentarlo."))
        (om/set-state! owner :errors errors))
      (do
        (om/set-state! owner :errors {})
        (om/set-state! owner :general-error nil)
        (om/set-state! owner :saving true)
        (when continuation
          (continuation))))
    (when continuation
      (continuation)))
   false)

(defn set-error
  [owner error-msg]
  (om/set-state! owner :general-error error-msg)
  (om/set-state! owner :saving false))

(defn set-success
  ([owner]
     (set-success owner nil))
  ([owner msg]
     (om/set-state! owner :saving false)
     (when msg
       (om/set-state! owner :success-message msg))
     (.scrollTo js/window 0 0)))
