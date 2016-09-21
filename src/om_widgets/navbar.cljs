(ns om-widgets.navbar
  (:import [goog.events EventType])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]
                   [pallet.thread-expr :as th])
  (:require [om-widgets.dropdown :refer [dropdown EntrySchema DividerSchema]]
            [schema.core :as s :include-macros true]
            [om.core :as om :include-macros true]
            [om-widgets.utils :as u]
            [cljs.core.async :refer [put! chan <! alts! timeout close!]]
            [sablono.core :refer-macros [html]]
            [om.dom :as dom :include-macros true]
            [goog.events :as events]))


;http://getbootstrap.com/css/#grid-media-queries
(def media-queries
  {:xs 0 ;Extra small devices (phones, less than 768px)
   :sm 768 ;Small devices (tablets, 768px and up)
   :md 992 ;Medium devices (desktops, 992px and up)
   :lg 1200 ;Large devices (large desktops, 1200px and up)
   })

(defn navbar-collapsed? [] (< (:width (u/get-window-boundaries!)) (:sm media-queries)))

(defn- entry-component
  [{:keys [cursor entry]} owner]
  (reify
    om/IRenderState
    (render-state [_ {:keys [channel on-selection active-path]}]
      (html
        [:li {:class (when (= (get-in cursor [active-path])
                              (:id entry))
                       "active")}

         (if (:items entry)
           (dropdown cursor (->> {:id (:id entry)
                                  :type :menu
                                  :icon (:icon entry)
                                  :title (:text entry)
                                  :on-selection (fn [v]
                                                  (on-selection v)
                                                  (when (om/get-state owner :collapsed?)
                                                    (put! channel :toggle-menu)))
                                  :items (:items entry)}))
           [:a (->> {}
                    (th/when->> (:className entry)
                                (merge {:className (:className entry)}))
                    (th/when->> (:url entry)
                                (merge {:href (:url entry)}))
                    (th/when->> on-selection
                                (merge {:onClick (fn [e]
                                                   (on-selection (:id @entry))
                                                   (when (om/get-state owner :collapsed?)
                                                     (put! channel :toggle-menu)))})))

            (when (or (:iconClassName entry) (:icon entry))
              [:span {:class (or (:iconClassName entry)
                                 (u/glyph (:icon entry)))}])

            [:span (:text entry)]

            (when (:badge entry)
              [:span.badge (:badge entry)])])]))))

(defn- navbar-nav
  [{:keys [cursor entries]} owner]
  (reify
    om/IRenderState
    (render-state [this {:keys [active-path on-selection channel collapsed?]}]
      (apply dom/ul #js {:className (str "nav navbar-nav"
                                         (when (:right-position (first entries))
                                           " navbar-right"))}
             (map #(om/build entry-component {:cursor cursor
                                              :entry %}
                             {:state {:active-path active-path
                                      :on-selection on-selection
                                      :channel channel
                                      :collapsed? collapsed?}})
                  entries)))))

(defn- nav-header
  [app owner]
  (reify
    om/IRenderState
    (render-state [this {:keys [collapsed? menu-hidden? channel] :as state}]
      (html
        [:div.navbar-header
         (when collapsed?
           [:button.navbar-toggle {:class (when menu-hidden? "collapsed")
                                   :onClick #(do (put! channel :toggle-menu) nil)}
            [:span.sr-only "Toggle navigation"]
            [:span.icon-bar]
            [:span.icon-bar]
            [:span.icon-bar]])

         [:a.navbar-brand {:href "#"}
          [:img {:src (:brand-image-url state)
                 :alt (or (:brand-title state) "brand-logo")
                 :height (if (:brand-image-expanded state) "100%" "")}]
          (str " " (:brand-title state))]]))))

(defn navbar-container
  [cursor owner]
  (reify
    om/IInitState
    (init-state [_]
      {:window-size {}
       :collapsed? false
       :menu-hidden? true
       :channel (chan)})

    om/IWillMount
    (will-mount [_]
      (go-loop []
        (let [msg (<! (om/get-state owner :channel))]
          (when-not (= :quit msg)
            (condp = msg
              :toggle-menu (om/set-state! owner :menu-hidden? (not (om/get-state owner :menu-hidden?)))))
          (recur))))

    om/IDidMount
    (did-mount [this]
      (om/set-state! owner :collapsed? (navbar-collapsed?))
      (events/listen js/window
                     EventType.RESIZE
                     #(om/set-state! owner :collapsed? (navbar-collapsed?))))

    om/IWillUnmount
    (will-unmount [this]
      (put! (om/get-state owner :channel) :quit))

    om/IRenderState
    (render-state [this {:keys [items collapsed? menu-hidden? channel] :as state}]
      (html
        [:nav {:class ["navbar"
                       "navbar-default"
                       (when collapsed? "collapsed")
                       (when (:fixed-top state) "navbar-fixed-top")]}

         [:div {:class (condp = (:container state)
                         :default "container"
                         :fluid "container-fluid"
                         "container")}

          (om/build nav-header
                    cursor
                    {:state {:brand-image-url (:brand-image-url state)
                             :brand-image-expanded (:brand-image-expanded state)
                             :brand-title (:brand-title state)
                             :channel channel
                             :collapsed? collapsed?
                             :menu-hidden? menu-hidden?}})

          (u/make-childs [:div {:class ["navbar-collapse"
                                        (when collapsed? "collapse")
                                        (when-not menu-hidden? "in")]}]
                         (map #(om/build navbar-nav {:cursor cursor
                                                     :entries %}
                                         {:state {:active-path (:active-path state)
                                                  :on-selection (:on-selection state)
                                                  :channel channel
                                                  :collapsed? collapsed?}})
                              items))]]))))

;; ---------------------------------------------------------------------
;; Schema

(def NavbarEntry
  {:text s/Str
   :id s/Keyword
   (s/optional-key :right-position) s/Bool
   (s/optional-key :url) s/Str
   (s/optional-key :className) s/Str
   (s/optional-key :badge) (s/either s/Int s/Str)
   (s/optional-key :icon) (s/either s/Keyword s/Str)})

(def NavbarDropdownEntry
  {:text s/Str
   :id s/Keyword
   :items [(s/either EntrySchema DividerSchema)]
   (s/optional-key :icon) (s/either s/Keyword s/Str)
   (s/optional-key :iconClassName) s/Str
   (s/optional-key :badge) (s/either s/Int s/Str)
   (s/optional-key :right-position) s/Bool})

(def NavbarSchema
  {:items [[(s/either NavbarEntry NavbarDropdownEntry)]]
   :brand-image-url s/Str
   :brand-title s/Str
   (s/optional-key :fixed-top) s/Bool
   (s/optional-key :brand-image-expanded) s/Bool
   (s/optional-key :container) (s/enum :default :fluid)
   (s/optional-key :on-selection) (s/pred fn?)})


;; ---------------------------------------------------------------------
;; Public

(defn navbar
  [app active-path {:keys [items brand-image-url brand-title on-selection] :as options}]
  (s/validate NavbarSchema options)
  (om/build navbar-container app {:state (merge {:active-path active-path} options)}))
