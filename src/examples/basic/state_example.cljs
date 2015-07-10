(ns examples.basic.state-example)


(def app-state
  (atom
    {:menu-selected :grid
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
                             :text "Grid Row Custom"}]}]
                  [{:text "Popup Window"
                    :id :popup-window
                    :url "#/popupwindow"}]]
     :birth-date #inst "1991-01-25"
     :sex :male
     :tab {}
     :form {:name ""
            :birth-date ""
            :sex :male
            :password ""}
     :grid {:source-simple [{:name "Sebas" :username "kernelp4nic"}
                            {:name "Guille" :username "guilespi"}
                            {:name "Fabian" :username "fapenia"}
                            {:name "Alexis" :username "_axs_"}
                            {:name "Martin" :username "nartub"}
                            {:name "Intception" :username "intception"}]

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
                :selected-dropdown :edit}}))
