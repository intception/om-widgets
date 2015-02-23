(ns examples.basic.state-example)


(def app-state
  (atom
    {:birth-date #inst "1991-01-25"
     :sex :male
     :grid {:source-simple [{:name "Sebas" :username "kernelp4nic"}
                            {:name "Guille" :username "guilespi"}
                            {:name "Fabian" :username "fapenia"}
                            {:name "Alexis" :username "_axs_"}
                            {:name "Martin" :username "nartub"}
                            {:name "Intception" :username "intception"}]

            :source-custom {:rows [{:name "Seba" :username "kernelp4nic" :row-type :users}
                                   {:name "Guille" :username "guilespi" :row-type :users}
                                   {:name "Fabian" :username "fapenia" :row-type :users}
                                   {:name "Alexis" :username "_axs_" :row-type :users}
                                   {:name "Martin" :username "nartub" :row-type :users}]}
            :selected {}
            :columns [{:caption "Name" :field :name}
                      {:caption "Username" :field :username}]
            }
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
                   {:id :modal
                    :text "Modal"
                    :url "#/modal"}
                   ]
                  [{:text "Grid"
                    :id :grid-sample
                    :items [{:id :grid
                             :type :entry
                             :text "Grid Simple"
                             :url "#/grid-simple"}
                            {:id :grid-link
                             :type :entry
                             :text "Grid With link"
                             }
                            {:id :grid-custom-row
                             :type :entry
                             :text "Grid Row Custom"
                             }]}]
                  [{:text "Popup Window"
                    :id :popup-window
                    :url "#/popupwindow" }
                   ]]}))