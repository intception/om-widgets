(ns examples.basic.form-example
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [sablono.core :as html :refer-macros [html]]
            [om-widgets.core :as w]))


(defn form-example
  [app owner]
  (reify
    om/IDisplayName
    (display-name[_] "FormSample")

    om/IRenderState
    (render-state [this state]
      (html
        [:div.panel.panel-default
         [:div.panel-heading "Form"]
         [:div.panel-body
           [:form
            [:div.form-group
             [:label "Name"]
             (w/textinput app :name {:input-class "form-control"
                                     :autofocus true
                                     :tabIndex 1
                                     :placeholder "Your name"})]

            [:div.form-group
             [:label "How old are you?"]
             (w/textinput app :age {:input-class "form-control"
                                    :autofocus true
                                    :tabIndex 2
                                    :input-format "numeric"
                                    :align "left"
                                    :placeholder "Your age (numbers only)"})]

            [:div.form-group
             [:label "Email"]
             (w/textinput app :email {:input-class "form-control"
                                      :tabIndex 3
                                      :placeholder "hello@domain.com"})]


            [:div.form-group
             [:label "How much hours do you work? (4-12)"]
             (w/slider app :hours {:step "1"
                                   :min "4"
                                   :max "12"})
             [:label (:hours app)]]

            [:div.form-group
             [:label "Birth Date"]
             (w/textinput app :birth-date {:input-class "form-control"
                                           :input-format "date"
                                           :tabIndex 4
                                           :placeholder "MM/DD/YYYY"})]

            [:div.form-group
             [:label "Sex"]
             (w/radiobutton-group app :sex {:class-name "some-container-class"
                                            :label-class "some-label-class"
                                            :options [{:label " Male"
                                                       :checked-value :male}
                                                      {:label " Female"
                                                       :checked-value :female}]})]

            [:div.form-group
             [:label "Marital Status"]
             [:br]
             (w/combobox app :marital-status
                         {:tabIndex 6
                          :options (sorted-map :single "Single"
                                               :married "Married"
                                               :divorced "Divorced"
                                               :widowed "Widowed")})]

            [:div.form-group
             [:label "Password"]
             (w/textinput app :password {:input-class "form-control"
                                         :tabIndex 7
                                         :flush-on-enter true
                                         :onEnter #(println "enter pressed!")
                                         :input-format "password"})]

            [:div.form-group
             [:label "Checkbox"]
             (w/checkbox app :some-check {:label "  Some Checkbox (Boolean)"
                                          :title "Some tooltip"
                                          :checked-value true
                                          :unchecked-value false})]


            [:div.form-group
             [:label "Checks - Toggle values on Sets"]
             (w/checkbox app :some-set {:label "check some values (keyword)"
                                        :id "some-value-set"
                                        :checked-value :some-value
                                        :toggle-value true})

             (w/checkbox app :some-set {:label "check some values (keyword)"
                                        :id "some-other-value-set"
                                        :checked-value :other-value
                                        :toggle-value true})]

            [:button.btn.btn-default {:type "submit"
                                      :tabIndex 8 } "Submit"]

            [:hr]
            [:div.form-group
             [:label "Cursor:"]
             [:p.form-control-static (pr-str app)]]]]]))))
