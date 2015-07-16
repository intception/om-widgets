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
             [:label "Email"]
             (w/textinput app :email {:input-class "form-control"
                                      :tabIndex 2
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
                                           :tabIndex 3
                                           :placeholder "MM/DD/YYYY"})]

            [:div.form-group
             [:label "Sex"]
             (w/radiobutton app :sex {:checked-value :female
                                      :tabIndex 4
                                      :class-name "some-container-class"
                                      :label-class "some-label-class"
                                      :label " Female"})
             (w/radiobutton app :sex {:checked-value :male
                                      :class-name "some-container-class"
                                      :label-class "some-label-class"
                                      :label " Male"})]

            [:div.form-group
             [:label "Marital Status"]
             [:br]
             (w/combobox app :marital-status
                             {:tabIndex 5
                              :options (sorted-map :single "Single"
                                                   :married "Married"
                                                   :divorced "Divorced"
                                                   :widowed "Widowed")})]

            [:div.form-group
             [:label "Password"]
             (w/textinput app :password {:input-class "form-control"
                                         :tabIndex 6
                                         :input-format "password"})]

            [:button.btn.btn-default {:type "submit"
                                      :tabIndex 7} "Submit"]

            [:hr]
            [:div.form-group
             [:label "Cursor:"]
             [:p.form-control-static (pr-str app)]]]]]))))
