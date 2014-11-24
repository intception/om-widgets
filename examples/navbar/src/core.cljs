(ns examples.basic.core
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [intception-widgets.core :as w]))

(enable-console-print!)

(def app-state (atom {:menu-selected :dashboard
                      :menu-items [[{:text "Dashbaord"
                                     :id :dashboard
                                     :url "#/dashboard"
                                     }
                                    {:id :getting-started
                                     :text "Gettin Started"
                                     :url "#/getting-started"
                                     }]
                                   [{:text "Profile"
                                     :right-position true
                                     :id :profile
                                     :items [{:id :profile
                                              :type :entry
                                              :text "User profile"
                                              :url "#/profile"}
                                             {:id :logout
                                              :type :entry
                                              :text "Logout"
                                              :url "#/logout"}]}]]}))

(defn my-app [app owner]
  (reify
    om/IDisplayName
    (display-name[_] "App")
    om/IInitState
    (init-state [_]
                {:hide-dropdown true})
    om/IRenderState
    (render-state [this state]
                  (dom/div #js {:className ""}
                           (w/navbar app-state {:items (get-in app [:menu-items])
                                                :selected (get-in app [:menu-selected])
                                                :brand-image-url "images/logo.png"
                                                :brand-title "Your company"})))))

(om/root my-app
         app-state
         {:target (.getElementById js/document "app")})
