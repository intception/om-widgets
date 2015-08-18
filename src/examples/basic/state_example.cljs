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
            :hours 8
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
