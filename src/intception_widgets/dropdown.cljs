(ns intception-widgets.dropdown
  (:require
    [om.core :as om :include-macros true]
    [om.dom :as dom :include-macros true]))

;; TODO support headings: <li class="dropdown-header">Nav header</li>
;; TODO support dividers: <li class="divider"></li>

(defn- dropdown-entry [item]
  (reify
    om/IDisplayName
    (display-name[_] "DropdownEntry")

    om/IRenderState
    (render-state [this state]
                  (dom/li nil
                          (dom/a #js {:href (:url item)} (:text item))))))

(defn- dropdown-menu-container [app owner]
  (reify
    om/IDisplayName
    (display-name[_] "DropdownMenu")

    om/IInitState
    (init-state [_]
                {:opened false})
    om/IRenderState
    (render-state [this {:keys [id title items] :as state}]
                  (dom/li #js {:className (str "dropdown " (when (:opened state) "open"))
                               :onClick #(om/set-state! owner :opened (not (:opened state)))}
                          (dom/a #js {:className "dropdown-toggle"
                                      :data-toggle "dropdown"} title
                                 (dom/span #js {:className "caret"}))
                          (apply dom/ul #js {:className "dropdown-menu" :role "menu"}
                                 (om/build-all dropdown-entry (:items state)))))))

(defn- dropdown-container [app owner]
  (reify
    om/IDisplayName
    (display-name[_] "Dropdown")

    om/IInitState
    (init-state [_]
                {:opened false})
    om/IRenderState
    (render-state [this {:keys [id title items] :as state}]
                  (dom/div #js {:className (str "dropdown " (when (:opened state) "open"))
                                :onClick #(om/set-state! owner :opened (not (:opened state)))}

                           (dom/button #js {:className "btn btn-default btn-xs dropdown-toggle"
                                            :type "button"
                                            :id (str "dropdown-" id)
                                            :data-toggle "dropdown"} title
                                       (dom/span #js {:className "caret"}))
                           (apply dom/ul #js {:className "dropdown-menu"
                                              :aria-labelledby (str "dropdown-" id)
                                              :role "menu"}
                                  (om/build-all dropdown-entry (:items state)))))))

(defn dropdown
  "items example:

  [{:id :logout
  :text 'Logout'
  :url '#/logout'}

  {:id :profile
  :text 'Profile'
  :url '#/profile'}]
  "
  [app id title items]
  (om/build dropdown-container app {:state {:id id
                                            :title title
                                            :items items}}))

(defn dropdown-menu
  "Same as dropdown but with a few markup differences"
  [app id title items]
  (println app id title items)
  (om/build dropdown-menu-container app {:state {:id id
                                                 :title title
                                                 :items items}}))
