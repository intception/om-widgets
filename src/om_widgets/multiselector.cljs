(ns om-widgets.multiselector
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require
    [om.core :as om :include-macros true]
    [om-widgets.core :as w]
    [om-widgets.utils :as u]
    [om-widgets.popover :refer [popover-container]]
    [dommy.core :refer-macros [sel sel1]]
    [cljs.core.async :refer [put! chan <! alts! timeout close!]]
    [sablono.core :refer-macros [html]]))


(defn multi-selector-popover
  [cursor owner]
  (reify

    om/IWillMount
    (will-mount [_]
      (go
        (let [path (om/get-state owner :path)]
          (when (nil? (get cursor path))
            (om/transact! cursor #(assoc % path #{}))))))

    om/IDidMount
    (did-mount [this]
      (go
        (<! (timeout 50))
        (->> (str "#multi-selector-input") (sel1 (om/get-node owner)) (.focus))))

    om/IRenderState
    (render-state [this {:keys [options search-value path on-change disable-select-all? channel] :as state}]
      (html
        [:div
         [:div {:class "popover-header"}
          [:div {:class "form-group has-icon"}
           (w/textinput owner :search-value {:input-class "form-control input-sm"
                                             :typing-timeout 100
                                             :id "multi-selector-input"
                                             :onChange #(go
                                                         (<! (timeout 50))
                                                         (put! channel :update))})
           [:div {:class "form-control-feedback"}
            [:span {:class "icn-magnifier"}]]]]
         (u/make-childs [:ul {:class "list-bordered multi-content list-unstyled"}]
                        (->> options
                             (filter (fn [[v n]]
                                       (if search-value
                                         (re-find (re-pattern (str "(?i)" (u/re-quote search-value))) (pr-str n))
                                         true)))
                             (map-indexed
                               (fn [idx [v n]]
                                 [:li
                                  (w/checkbox cursor path
                                              {:label n
                                               :id (str "multi-selector-" idx)
                                               :toggle-value true
                                               ;; TODO move this behaviour outside multiselector component
                                               :disabled (and (:at-least-one-checked? state)
                                                              (= 1 (count (get-in cursor [path])))
                                                              (contains? (get-in cursor [path]) v))
                                               :checked-value v})]))))
         (when-not disable-select-all?
           [:div {:class "popover-footer"}
            [:div {:class "pull-right"}
             [:a {:class "title"
                  :onClick #(do
                             (om/update! cursor path #{})
                             (.preventDefault %))}
              "Unselect All"]]
            [:a {:class "title"
                 :onClick #(do
                            (->> (map first options)
                                 set
                                 (om/update! cursor path))
                            (.preventDefault %))}
             "Select All"]])]))))

(defn multiselector-component
  [cursor owner]
  (reify
    om/IInitState
    (init-state [_]
      {:channel (chan)})

    om/IDidUpdate
    (did-update [_ prev-props prev-state]
      (let [path (om/get-state owner :path)
            on-change (om/get-state owner :on-change)]
        (when (and on-change
                   (not= (get prev-props path)
                         (get (om/get-props owner) path)))
          (on-change (get (om/get-props owner) path)))))

    om/IRenderState
    (render-state [this {:keys [class-name label disabled channel visible] :as state}]
      (html
        (w/popover label
                   (fn [close]
                     (om/build multi-selector-popover cursor {:state state}))
                   {:popover-class (str "multi-select " (or class-name "multi-select-sm"))
                    :channel channel
                    :for (:id state)
                    :visible visible})))))


(defn multiselector
  [cursor path {:keys [options class-name id on-change tab-index disabled label disable-select-all? at-least-one-checked? visible]}]
  (om/build multiselector-component cursor
            {:state {:options options
                     :class-name class-name
                     :id id
                     :label label
                     :visible visible
                     :on-change on-change
                     :disable-select-all? disable-select-all?
                     :at-least-one-checked? at-least-one-checked?
                     :path path
                     :disabled disabled
                     :tab-index tab-index}}))
