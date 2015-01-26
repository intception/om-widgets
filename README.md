# om-widgets

A Clojure clojurescript library that implement om/react widgets.

[![Clojars Project](http://clojars.org/org.clojars.intception/om-widgets/latest-version.svg)](http://clojars.org/org.clojars.intception/om-widgets)


## License

Copyright © 2014 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.


## Samples

####Simple control usage


```clj
(ns example
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [schema.core :as s]
            [om-widgets.core :as widgets]))


(def app-state (atom {:text #inst "2012-06-01" :current-item {}}))

(om/root
  (fn [app owner]
    (reify om/IRender
      (render [_]
        (dom/div nil
          (widgets/install-modal-box! owner)
          (dom/h1 nil (str (:text app)))
          (widgets/textinput app :text :input-format "date")
```

####Form


```clj
(ns om-widgets-tests.screen
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [schema.core :as s]
            [om-widgets.core :as widgets]
            [om-widgets.forms :as f :include-macros true]))

(def Rules
  {:name (s/pred #(not (empty? %)) 'RequiredName)})

(def Messages
  {:name "The name is required"})

(om/root
  (fn [app owner]
    (reify
      om/IInitState
      (init-state [_]
      {:errors {}})

      om/IRenderState
      (render-state [_ {:keys [errors ] :as state}]

        (f/form app owner {:title "Test form"
                           :subtitle "People"
                           :errors (or errors {})

                           :validation {:messages Messages
                                        :rules Rules}}

          (f/field :name {:type :text :label "Name:" :required true })
          (f/field :birth-date {:type :text :label "Birth date:" :required true :input-format "date"})
          (f/field :nationality {:type :combo
                                 :label "Nationality:"
                                 :required true
                                 :options (sorted-map :nationality/legal "Legal"
                                                      :nationality/natural "Natural"
                                                      :nationality/permanent "Residencia permanente")})
          (f/section "Address")

          (f/row
            (f/column 4
              (f/field :street {:type :text :label "Street" }))
            (f/column 2
              (f/field :number {:type :text :label "Number" }))
            (f/column 2
              (f/field :apartment {:type :text :label "Apartment"  })))

          (f/button :btn-save { :icon :save
                                :text "Save"
                                :on-valid (fn [e])})))))
  app-state
  {:target (. js/document (getElementById "app"))})
```
