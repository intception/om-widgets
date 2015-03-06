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
                                     :tabindex 1
                                     :placeholder "Your name"})]

            [:div.form-group
             [:label "Email"]
             (w/textinput app :email {:input-class "form-control"
                                      :tabindex 2
                                      :placeholder "hello@domain.com"})]

            [:div.form-group
             [:label "Birth Date"]
             (w/textinput app :birth-date {:input-class "form-control"
                                           :input-format "date"
                                           :tabindex 3
                                           :placeholder "MM/DD/YYYY"})]

            [:div.form-group
             [:label "Sex"]
             (w/radiobutton app :sex {:checked-value :female
                                      :tabindex 4
                                      :class-name "some-container-class"
                                      :label-class "some-label-class"
                                      :label " Female"})
             (w/radiobutton app :sex {:checked-value :male
                                      :tabindex 5
                                      :class-name "some-container-class"
                                      :label-class "some-label-class"
                                      :label " Male"})]

            [:div.form-group
             [:label "Password"]
             (w/textinput app :password {:input-class "form-control"
                                         :tabindex 6
                                         :input-format "password"})]

            [:button.btn.btn-default {:type "submit"
                                      :tabindex 7} "Submit"]]]]))))