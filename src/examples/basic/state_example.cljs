(ns examples.basic.state-example
  (:require [sablono.core :as html :refer-macros [html]]))

(def app-state
  (atom
    {:menu-selected :form
     :menu-items [[{:text "Form"
                    :id :form
                    :url "#/form"}
                   {:text "Dropdown"
                    :id :dropdown
                    :url "#/dropdown"}
                   {:text "Tabs"
                    :id :tab
                    :url "#/tab"}
                   {:id :datepicker
                    :text "Datepicker"
                    :url "#/datepicker"}
                   {:id :modal
                    :text "Modal"
                    :url "#/modal"}]
                  [{:text "Grid"
                    :id :grid-sample
                    :items [{:id :grid
                             :type :entry
                             :text "Grid Simple"
                             :url "#/grid-simple"}
                            {:id :grid-link
                             :type :entry
                             :text "Grid With Custom Cells"}
                            {:id :grid-custom-row
                             :type :entry
                             :text "Grid Row Custom"}

                            {:id :grid-request-range
                             :type :entry
                             :text "Grid Request Range"}
                            {:id :grid-summarized
                             :type :entry
                             :text "Grid Summarized"}

                            ]}]
                  [{:text "Popup Window"
                    :id :popup-window
                    :url "#/popupwindow"}]

                  [{:text "Notifications"
                    :icon :bell
                    :badge 10
                    :id :notifications
                    :items [{:id :users
                             :type :entry
                             :icon :user
                             :text "Users"}
                            {:id :inbox
                             :type :entry
                             :icon :inbox
                             :badge 6
                             :text "Inbox"}]}]
                  [{:text "Editable list"
                    :id :editable-list}]
                  [{:text (fn [] (html [:label "Current user"]))
                    :className "profile-user"
                    :id :profile
                    :right-position true
                    :items [{:id :profile
                             :type :entry
                             :text "Profile"}
                            {:id :logout
                             :type :entry
                             :text "Logout"}]}]]
     :datepicker {:inline #inst "1991-01-25"
                  :input-group-left #inst "1991-01-25"
                  :input-group-right #inst "1991-01-25"
                  :input-group-close-on-change #inst "1991-01-25"}
     :sex :male
     :tab {:selected-tab :inbox}
     :form {:name ""
            :age 25
            :decimal 1.5
            :birth-date ""
            :hours 8
            :sex :male
            :password ""
            :some-set #{}}
     :grid {:request-range {}
            :source-simple [{:name "Sebas" :username "kernelp4nic"  :fecha #inst "2002-04-20"}
                            {:name "Guille" :username "guilespi" :fecha #inst "2004-04-20"}
                            {:name "Fabian" :username "fapenia" :fecha #inst "2004-04-21"}
                            {:name "Alexis" :username "_axs_" :fecha #inst "2012-04-20"}
                            {:name "Martin" :username "nartub" :fecha #inst "2005-02-17"}
                            {:name "Intception" :username "intception" :fecha #inst "2001-02-17"}]

            :source-summarized [{:name "Sebas" :username "kernelp4nic"  :fecha #inst "2002-04-20" :total 1000}
                                {:name "Guille" :username "guilespi" :fecha #inst "2004-04-20" :total 900}
                                {:name "Fabian" :username "fapenia" :fecha #inst "2004-04-21" :total 920}
                                {:name "Alexis" :username "_axs_" :fecha #inst "2012-04-20" :total 500}
                                {:name "Martin" :username "nartub" :fecha #inst "2005-02-17" :total 3000}
                                {:name "Intception" :username "intception" :fecha #inst "2001-02-17" :total 2340}]

            :source-custom-cell [{:name "Seba"
                                  :username "kernelp4nic"
                                  :registered-date #inst "2015-01-25"
                                  :status :active}
                                 {:name "Guille"
                                  :username "guilespi"
                                  :registered-date #inst "2014-01-25"
                                  :status :disabled}]

            :source-custom {:rows [{:name "Seba" :username "kernelp4nic" :row-type :users}
                                   {:name "Guille" :username "guilespi" :row-type :users}
                                   {:name "Fabian" :username "fapenia" :row-type :users}
                                   {:name "Alexis" :username "_axs_" :row-type :users}
                                   {:name "Martin" :username "nartub" :row-type :users}]}
            :selected {}
            :persist-paging {}
            :current-page 0
            :multiselect #{}
            }
     :editable-list-with-strings []
     :editable-list-with-dates   []
     :editable-list-with-numbers   []


     :dropdown {:urls [{:id :first-link :type :entry :text "First Link" :url "#/link/1"}
                       {:id :second-link :type :entry :text "Second Link" :url "#/link/2"}
                       {:id :third-link :type :entry :text "Third Link" :url "#/link/3"}
                       {:id :fourth-link :type :entry :text "Fourth Link" :url "#/link/4"}]

                :dividers [{:id :entry1 :type :entry :text "Entry 1"}
                           {:type :divider}
                           {:id :entry2 :type :entry :text "Entry 2"}
                           {:id :entry3 :type :entry :text "Entry 3"}
                           {:type :divider}
                           {:id :entry4 :type :entry :text "Entry 3"}]

                :default [{:id 1 :type :entry :text "Entry 1"}
                          {:id 2 :type :entry :text "Entry 2"}
                          {:id 3 :type :entry :text "Entry 3"}
                          {:id 4 :type :entry :text "Entry 4"}]

                :disabled [{:id 1 :type :entry :text "Entry 1"}
                           {:id 2 :type :entry :text "Entry 2" :disabled true}
                           {:id 3 :type :entry :text "Entry 3"}
                           {:id 4 :type :entry :text "Entry 4" :disabled true}]

                :selected-dropdown :edit}}))
