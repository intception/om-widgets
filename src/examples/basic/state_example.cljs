(ns examples.basic.state-example)


(def app-state
  (atom
    {:menu-selected              :placeholder-editor
     :menu-items                 [[{:text "Form"
                                    :id   :form
                                    :url  "#/form"}
                                   {:text "Dropdown"
                                    :id   :dropdown
                                    :url  "#/dropdown"}
                                   {:id   :datepicker
                                    :text "Datepicker"
                                    :url  "#/datepicker"}
                                   {:id   :modal
                                    :text "Modal"
                                    :url  "#/modal"}]
                                  [{:text  "Grid"
                                    :id    :grid-sample
                                    :items [{:id   :grid
                                             :type :entry
                                             :text "Grid Simple"
                                             :url  "#/grid-simple"}
                                            {:id   :grid-link
                                             :type :entry
                                             :text "Grid With Custom Cells"}
                                            {:id   :grid-custom-row
                                             :type :entry
                                             :text "Grid Row Custom"}]}]
                                  [{:text "Popup Window"
                                    :id   :popup-window
                                    :url  "#/popupwindow"}]
                                  [{:text "Placeholder editor"
                                    :id   :placeholder-editor
                                    :url  "#/placeholdereditor"}]
                                  ]
     :birth-date                 #inst "1991-01-25"
     :sex                        :male
     :form                       {:name       ""
                                  :birth-date ""
                                  :sex        :male
                                  :password   ""}
     :grid                       {:source-simple      [{:name "Sebas" :username "kernelp4nic"}
                                                       {:name "Guille" :username "guilespi"}
                                                       {:name "Fabian" :username "fapenia"}
                                                       {:name "Alexis" :username "_axs_"}
                                                       {:name "Martin" :username "nartub"}
                                                       {:name "Intception" :username "intception"}]

                                  :source-custom-cell [{:name            "Seba"
                                                        :username        "kernelp4nic"
                                                        :registered-date #inst "2015-01-25"
                                                        :status          :active}
                                                       {:name            "Guille"
                                                        :username        "guilespi"
                                                        :registered-date #inst "2014-01-25"
                                                        :status          :disabled}]

                                  :source-custom      {:rows [{:name "Seba" :username "kernelp4nic" :row-type :users}
                                                              {:name "Guille" :username "guilespi" :row-type :users}
                                                              {:name "Fabian" :username "fapenia" :row-type :users}
                                                              {:name "Alexis" :username "_axs_" :row-type :users}
                                                              {:name "Martin" :username "nartub" :row-type :users}]}
                                  :selected           {}
                                  :columns            [{:caption "Name" :field :name}
                                                       {:caption "Username" :field :username}]}
     :dropdown                   {:items             [{:id   :duplicate
                                                       :type :entry
                                                       :text "Duplicate"
                                                       :url  "#/item/duplicate/1234"}
                                                      {:type :divider}
                                                      {:id   :analysis
                                                       :type :entry
                                                       :text "Analysis"
                                                       :url  "#/item/analysis/1234"}
                                                      {:type :divider}
                                                      {:id   :trash
                                                       :type :entry
                                                       :text "Trash"
                                                       :url  "#/item/trash/1234"}]
                                  :selected-dropdown :edit}
     :placeholder-editor-example {:banner {:placeholders #{{:name     "E-Banking - Front", :width 400
                                                            :id       17592186045518,
                                                            :company  {:name "chase", :id 17592186045486},
                                                            :platform :platform/mobile,
                                                            :height   150}},
                                           :images       [
                                                          {:db-id    17592186046981,
                                                           :width    400,
                                                           :mimetype "image/png",
                                                           :size     11379,
                                                           :filename "mobile.png",
                                                           :id       "dc9a4c67-08cd-41d6-ad1c-d02663bc4af4",
                                                           :db/id    17592186046981,
                                                           :file/id  "dc9a4c67-08cd-41d6-ad1c-d02663bc4af4",
                                                           :height   150}
                                                          {:db-id    17592186046977,
                                                           :width    1400,
                                                           :mimetype "image/png",
                                                           :size     13478,
                                                           :filename "blue.png",
                                                           :id       "3d09c9de-93d7-4c13-b514-530e4f2e3b4f",
                                                           :db/id    17592186046977,
                                                           :file/id  "3d09c9de-93d7-4c13-b514-530e4f2e3b4f",
                                                           :height   150}],
                                           :name         "eneltelefono",
                                           :image-map    {
                                                          17592186045518 [
                                                                          {:image      17592186046981
                                                                           :overprints [{:type        :text
                                                                                         :text        "LLame ya!"
                                                                                         :font-family "verdana"
                                                                                         :font-size   24
                                                                                         :font-weight "bold"
                                                                                         :color       "#FF000070"
                                                                                         :text-align  "center"
                                                                                         :background  "transparent"
                                                                                         :font-style  "italic"
                                                                                         :width       150
                                                                                         :height      90
                                                                                         :top         0
                                                                                         :left        0}
                                                                                        {:type        :text
                                                                                         :text        "Y obtenga un segundo pote de regalo!"
                                                                                         :font-family "verdana"
                                                                                         :font-size   24
                                                                                         :text-align  "left"
                                                                                         :font-weight "bold"
                                                                                         :color       "#0000FF"
                                                                                         :font-style  "italic"
                                                                                         :background  "transparent"
                                                                                         :width       150
                                                                                         :height      90
                                                                                         :top         60
                                                                                         :left        30
                                                                                         }]
                                                                           }
                                                                          {:image      17592186046977
                                                                           :overprints [{:type        :text
                                                                                         :text        "Otro ab test"
                                                                                         :font-family "verdana"
                                                                                         :font-size   24
                                                                                         :font-weight "bold"
                                                                                         :color       "#FF000070"
                                                                                         :text-align  "center"
                                                                                         :background  "transparent"
                                                                                         :font-style  "italic"
                                                                                         :width       150
                                                                                         :height      90
                                                                                         :top         0
                                                                                         :left        0}
                                                                                      ]
                                                                           }
                                                                          ]
                                                          },
                                           :id           17592186046932, :order 1}}
     }))
