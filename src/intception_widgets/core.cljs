(ns intception-widgets.core
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]

            [intception-widgets.stylesheet]
            [intception-widgets.textinput]
            [intception-widgets.checkbox]
            [intception-widgets.radiobutton]
            [intception-widgets.combobox]
            [intception-widgets.grid]
            [intception-widgets.modal-box]
            [intception-widgets.button]
            [intception-widgets.tab]

            ))

(def textinput intception-widgets.textinput/textinput)
(def checkbox intception-widgets.checkbox/checkbox)
(def radiobutton intception-widgets.radiobutton/radiobutton)
(def radiobutton-group intception-widgets.radiobutton/radiobutton-group)
(def combobox intception-widgets.combobox/combobox)
(def button intception-widgets.button/button)
(def grid intception-widgets.grid/grid)
(def tab intception-widgets.tab/tab)
(def install-modal-box! intception-widgets.modal-box/install-modal-box!)
(def alert intception-widgets.modal-box/alert)
(def ok-cancel intception-widgets.modal-box/ok-cancel)
(def do-modal intception-widgets.modal-box/do-modal)

