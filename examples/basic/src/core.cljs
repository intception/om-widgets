(ns examples.basic.core
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [widgets.layouts :as layout :include-macros true]
            [intception-widgets.core :as w]
            [intception-widgets.grid :refer [row-builder]]))


(enable-console-print!)

(def app-state
  (atom
    {:birth-date #inst "1991-01-25"
     :sex :male
     :grid {:source-simple [{:name "Seba" :username "kernelp4nic"}
                            {:name "Guille" :username "guilespi"}
                            {:name "Fabian" :username "fapenia"}
                            {:name "Alexis" :username "_axs_"}
                            {:name "Martin" :username "nartub"}]
            :source-custom {:rows [{:name "Seba" :username "kernelp4nic" :row-type :users}
                                   {:name "Guille" :username "guilespi" :row-type :users}
                                   {:name "Fabian" :username "fapenia" :row-type :users}
                                   {:name "Alexis" :username "_axs_" :row-type :users}
                                   {:name "Martin" :username "nartub" :row-type :users}]}
            :selected {}
            :columns [{:caption "Name" :field :name}
                      {:caption "Username" :field :username}]}
     :dropdown {:items [{:id :duplicate
                         :type :entry
                         :text "Duplicate"
                         :url "#/item/duplicate/1234"}
                        {:type :divider}
                        {:id :analysis
                         :type :entry
                         :text "Analysis"
                         :url "#/item/analysis/1234"}
                        {:type :divider}
                        {:id :trash
                         :type :entry
                         :text "Trash"
                         :url "#/item/trash/1234"}]
                :selected-dropdown :edit}
     :menu-selected :grid
     :menu-items [[{:text "Dropdown"
                    :id :dropdown
                    :url "#/dropdown"}
                   {:id :datepicker
                    :text "Datepicker"
                    :url "#/datepicker"}
                   {:id :radiobutton
                    :text "Radiobutton"
                    :url "#/radiobutton"}
                   ]
                  [{:text "Grid"
                    :id :grid-sample
                    :items [{:id :grid
                             :type :entry
                             :text "Grid Simple"
                             :url "#/grid-simple"}
                            {:id :grid-custom-row
                             :type :entry
                             :text "Grid Row Custom"
                             }]}]]}))

(defn- datepicker-sample
  [app owner]
  (reify
    om/IInitState
    (init-state [_]
                {:hide-dropdown true})

    om/IDisplayName
    (display-name[_] "DatepickerSample")

    om/IRenderState
    (render-state [this state]
                  (dom/div #js {:className "panel panel-default"}
                           (dom/div #js {:className "panel-heading"} "Datepicker")
                           (dom/div #js {:className "panel-body"}
                                    (dom/div #js {:className "well"}
                                             (dom/div #js {:className "input-group"}
                                                      (dom/span #js {:className "input-group-btn"}
                                                                (dom/button #js {:className "btn btn-primary pull-right"
                                                                                 :type "button"
                                                                                 :onClick (fn [e]
                                                                                            (om/set-state! owner :hide-dropdown (not (:hide-dropdown state))))}
                                                                            (dom/span #js {:className "glyphicon glyphicon-calendar"})))
                                                      (w/datepicker app :birth-date {:hidden (:hide-dropdown state)})
                                                      (w/textinput app :birth-date {:input-class "form-control"
                                                                                    :input-format "date"
                                                                                    :placeholder "MM/DD/YYYY"}))))))))

(defn- radiobutton-sample
  [app owner]
  (reify
    om/IInitState
    (init-state [_]
                {:hide-dropdown true})

    om/IDisplayName
    (display-name[_] "RadioButtonSample")

    om/IRenderState
    (render-state [this state]
                  (dom/div #js {:className "panel panel-default"}
                           (dom/div #js {:className "panel-heading"} (str "RadioButton"
                                                                          " (cursor value "
                                                                          (get-in app [:sex])
                                                                          " )"))
                           (dom/div #js {:className "panel-body"}
                                    (dom/div #js {:className "well"}
                                             (dom/div #js {:className "row"}
                                                      (dom/div #js {:className "col-md-6 col-sm-6"}
                                                               (w/radiobutton app :sex {:checked-value :male
                                                                                        :name "options"
                                                                                        :class-name "some-container-class"
                                                                                        :label-class "some-label-class"
                                                                                        :data-toggle "buttons"
                                                                                        :label " Male"
                                                                                        :id "male"}))
                                                      (dom/div #js {:className "col-md-6 col-sm-6"}
                                                               (w/radiobutton app
                                                                              :sex
                                                                              {:checked-value :female
                                                                               :name "options"
                                                                               :class-name "some-container-class"
                                                                               :label-class "some-label-class"
                                                                               :data-toggle "buttons"
                                                                               :label " Female"
                                                                               :id "female"})))))))))

(defn- dropdown-sample
  [cursor owner]
  (reify
    om/IDisplayName
    (display-name [_] "DropdownSample")

    om/IRenderState
    (render-state [this state]
                  (dom/div #js {:className "panel panel-default"}
                           (dom/div #js {:className "panel-heading"}
                                    (str "Dropdown (selected cursor value: "
                                         (get-in cursor [:selected-dropdown])
                                         " )"))
                           (dom/div #js {:className "panel-body"}
                                    (w/dropdown cursor
                                                {:id :testing
                                                 :title "Item Actions"
                                                 :set-path :selected-dropdown
                                                 :size :sm
                                                 :items (get-in cursor [:items])}))))))

(defn- grid-sample
  [app owner]
  (reify
    om/IDisplayName
    (display-name[_] "GridSample")

    om/IRenderState
    (render-state [this state]
                  (dom/div #js {:className "panel panel-default"}
                           (dom/div #js {:className "panel-heading"} (str "Grid"
                                                                          " (selected cursor value: "
                                                                          (:name (get-in app [:selected]))
                                                                          " )"))
                           (dom/div #js {:className "panel-body"}
                                    (dom/div #js {:className ""}
                                             (w/grid (get-in app [:source-simple])
                                                     (get-in app [:selected])
                                                     :container-class-name ""
                                                     :header {:type :default
                                                              :columns (get-in app [:columns])})))))))

(defmethod row-builder :users
  [row _ _]
  (reify
    om/IDisplayName
    (display-name[_] "DefaultRow")
    om/IRenderState
    (render-state [this state]
                  (dom/div nil
                           (dom/label #js {:className ""} (str (:name row) " / " (:username row)))
                           (dom/a #js {:href (str "http://twitter.com/" (:username row))
                                       :className "pull-right"} "Twitter profile")))))

(defn- grid-custom-row-sample
  [app owner]
  (reify
    om/IDisplayName
    (display-name[_] "GridCustomRowSample")

    om/IRenderState
    (render-state [this state]
                  (dom/div #js {:className "panel panel-default"}
                           (dom/div #js {:className "panel-heading"} "Grid Custom Row")
                           (dom/div #js {:className "panel-body"}
                                    (dom/div #js {:className ""}
                                             (w/grid (get-in app [:source-custom :rows])
                                                     (get-in app [:selected])
                                                     :header {:type :none})))))))

(defn my-app [app owner]
  (reify
    om/IDisplayName
    (display-name[_] "App")

    om/IRenderState
    (render-state [this state]
                  (dom/div nil
                           (w/navbar app
                                     :menu-selected
                                     {:items (get-in app [:menu-items])
                                      :on-selection #(println "selected item: " %)
                                      :brand-image-url "images/logo.png"
                                      :brand-image-expanded true
                                      :brand-title "Navbar Sample"})

                           (condp = (:menu-selected app)
                             :dropdown (om/build dropdown-sample (get-in app [:dropdown]))
                             :datepicker (om/build datepicker-sample app)
                             :grid (om/build grid-sample (get-in app [:grid]))
                             :grid-custom-row (om/build grid-custom-row-sample (get-in app [:grid]))
                             :radiobutton (om/build radiobutton-sample app))))))

(om/root
  my-app
  app-state
  {:target (.getElementById js/document "app")})

