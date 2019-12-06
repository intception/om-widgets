(ns om-widgets.combobox-searchable
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [om.core :as om :include-macros true]
            [om-widgets.popover :refer [popover-container]]
            [cljs.core.async :refer [put! chan <! alts! timeout close!]]
            [sablono.core :refer-macros [html]]
            [dommy.core :refer-macros [sel sel1]]
            [om-widgets.core :as w]
            [pallet.thread-expr :as th]))

(defn re-quote [s]
  (let [special (set ".?*+^$[]\\(){}|")
        escfn #(if (special %) (str \\ %) %)]
    (apply str (map escfn s))))

(defn- entry
  [target owner]
  (reify
    om/IRenderState
    (render-state
      [this {:keys [value name channel divider-class disabled? active?]}]
      (html
        (cond
          (= value :custom-html)
          (html name)

          (= value :divider)
          [:li {:class (or divider-class "divider")}
           (if (vector? name)
             (html name)
             (when (string? name)
               name))]

          :else
          [:li {:class (when active? "active")}
           [:a {:disabled disabled?
                :onClick #(put! channel {:type :set
                                         :value value})}
            (if (vector? name)
              (html name)
              (str name))]])))))

(defn- body
  [target owner]
  (reify
    om/IDidMount
    (did-mount [this]
      (when (om/get-state owner :searchable?)
        (go
          (<! (timeout 50))
          (->> (str "#combo-search-input")
               (sel1 (om/get-node owner))
               (.focus)))))

    om/IRenderState
    (render-state [_ {:keys [options loading? loading-view typing-timeout] :as state}]
      (html
        [:div
         (when (:searchable? state)
           [:div {:class "popover-header"}
            [:div {:class "form-group"}
             (w/textinput owner :search-value {:input-class "form-control input-sm"
                                               :flush-on-enter true
                                               :typing-timeout (or typing-timeout 50)
                                               :onChange #(put! (:channel state) {:type :search
                                                                                  :value %})
                                               :id "combo-search-input"})
             [:div {:class "form-control-feedback"}
              [:span {:class "icn-magnifier"}]]]])

         (cond
           (and loading?
                (fn? loading-view))
           (loading-view)

           (empty? options)
           [:ul
            [:li [:div {:class "padding-block"}
                  [:span {:class "body-lighter body-sm-i text-center"}
                   "No results found"]]]]

           :else
           (->> options
                (map (fn [[value name]]
                       (om/build entry target {:state {:path (:path state)
                                                       :divider-class (:divider-class state)
                                                       :value value
                                                       :active? (= value (:selected state))
                                                       :disabled? true
                                                       :name name
                                                       :channel (:channel state)}})))
                (concat [:ul {:class ["dropdown-menu" (:list-class state)]
                              :role "menu"}])
                vec))]))))


(defn- handle-keydown
  [owner event options]
  (let [ESC 27
        UP 38
        DOWN 40
        ENTER 13
        k (.-keyCode event)
        channel (om/get-state owner :channel)
        index (om/get-state owner :selected-index)]
    (when (contains? #{ESC UP DOWN ENTER} k)
      (condp = k
        UP (let [new-index (if (pos? index)
                             (dec index)
                             0)]
             (om/set-state! owner :selected (first (nth options new-index)))
             (om/set-state! owner :selected-index new-index)
             (.preventDefault event))
        DOWN (let [size (- (count options) 1)
                   new-index (if (>= index size)
                               size
                               (inc index))]
               (om/set-state! owner :selected (first (nth options new-index)))
               (om/set-state! owner :selected-index new-index)
               (.preventDefault event))
        ENTER (let [selected (om/get-state owner :selected)]
                (when selected (put! channel {:type :set
                                              :value selected})))
        ESC (.stopPropagation event)))))


(defn open-button
  [target owner]
  (reify
    om/IRenderState
    (render-state [_ {:keys [as-link? channel opened onClick icon disabled path default label placeholder options label-key]}]
      (html
        [:button (-> {:type "button"
                      :class ["btn" (if as-link?
                                      "btn-link"
                                      "btn-default dropdown-toggle")]
                      :onClick #(do (put! channel {:type :open
                                                   :open (not opened)})
                                    (when (fn? onClick)
                                      (onClick (not opened))))}
                     (merge (when disabled
                              {:disabled true})))

         (when icon
           [:span {:class icon}])

         [:span
          (cond
            label label

            (and target (not (nil? (get-in target path))) label-key)
            (get-in target (conj path label-key))

            (and target (or (not (nil? (get-in target path))) default))
            (get (into {} options)
                 (if (not (nil? (get-in target path)))
                   (get-in target path)
                   default))

            :else
            placeholder)]

         (when-not as-link?
           [:span {:class "caret"}])]))))



(defn- combobox-component
  [target owner {:keys [class-name divider-class list-class popover-class]}]
  (reify
    om/IInitState
    (init-state [_]
      {:opened false
       :search-value nil
       :selected nil
       :selected-index -1
       :channel (chan)})

    om/IWillMount
    (will-mount [_]
      (om/set-state! owner :filtered-options (om/get-state owner :options))
      (let [path (om/get-state owner :path)
            on-change (om/get-state owner :onChange)
            channel (om/get-state owner :channel)]
        (go-loop []
                 (let [msg (<! channel)]

                   (condp = (:type msg)
                     :set
                     (do
                       (when (om/get-props owner)
                         (om/update! (om/get-props owner) path (:value msg)))
                       (when on-change
                         (on-change (:value msg)))

                       (om/update-state! owner #(merge % {:search-value nil
                                                          :selected nil
                                                          :opened false})))

                     :open (om/set-state! owner :opened (:open msg))

                     :search
                     (om/set-state! owner :search-value (:value msg)))
                   (recur)))))

    om/IRenderState
    (render-state
      [this {:keys [id path opened disabled label placeholder as-link? icon onBlur onClick default channel] :as state}]
      (let [options (->> (:options state)
                         (filter (fn [[v n s]]
                                   (if (and (:search-value state)
                                            (not (clojure.string/blank? (:search-value state))))
                                     (re-find (re-pattern (str "(?i)" (re-quote (:search-value state))))
                                              (or s
                                                  (pr-str n)))
                                     true))))]
        (html
          [:div (merge {:class (str "btn-group " (when opened "open ") (or class-name "btn-group-xs"))
                        :onKeyDown (fn [event] (handle-keydown owner event options))}
                       (when id
                         {:id id}))

           (om/build open-button target {:state {:channel channel
                                                 :path path
                                                 :disabled disabled
                                                 :label label
                                                 :options options
                                                 :placeholder placeholder
                                                 :as-link? as-link?
                                                 :icon icon
                                                 :onClick onClick
                                                 :default default}})

           (when opened
             (om/build popover-container
                       nil
                       {:state {:content-fn #(om/build body target {:state (merge state
                                                                                  {:options options}
                                                                                  {:divider-class divider-class
                                                                                   :list-class list-class})})
                                :prefered-side :bottom}
                        :opts {:align 0
                               :mouse-down #(do
                                              (om/update-state! owner (fn [s] (merge s {:opened false
                                                                                        :search-value nil
                                                                                        :selected nil
                                                                                        :selected-index -1})))
                                              (when onBlur
                                                (onBlur)))
                               :popover-class (str "combobox"
                                                   (when popover-class
                                                     (str " " popover-class)))}}))])))))


;; ---------------------------------------------------------------------
;; Public

(defn combobox
  [target path {:keys [id label options onChange onBlur onClick disabled
                       placeholder class-name as-link? icon opened searchable?
                       divider-class popover-class list-class default loading? loading-view]}]
  (om/build combobox-component target
            {:state (merge {:id id
                            :path (if (sequential? path) path [path])
                            :placeholder placeholder
                            :as-link? as-link?
                            :icon icon
                            :loading? loading?
                            :loading-view loading-view
                            :searchable? searchable?
                            :disabled disabled
                            :onChange onChange
                            :onClick onClick
                            :onBlur onBlur
                            :default default
                            :label label
                            :options options}
                           (when (some? opened)
                             {:opened opened}))
             :opts {:class-name class-name
                    :divider-class divider-class
                    :popover-class popover-class
                    :list-class list-class}}))


(defn async-combobox
  [target owner {:keys [class-name divider-class list-class]}]
  (reify
    om/IInitState
    (init-state [_]
      {:opened false
       :search-value nil
       :selected nil
       :selected-index -1
       :local-channel (chan)})

    om/IWillMount
    (will-mount [_]
      (let [path (om/get-state owner :path)
            on-change (om/get-state owner :onChange)]
        (go-loop []
                 (let [msg (<! (om/get-state owner :local-channel))]
                   (condp = (:type msg)
                     :set
                     (do
                       (when (om/get-props owner)
                         (om/update! (om/get-props owner) path (:value msg)))

                       (when on-change
                         (on-change (:value msg)))

                       (om/update-state! owner #(merge % {:search-value nil
                                                          :selected nil
                                                          :opened false})))

                     :open (om/set-state! owner :opened (:open msg))

                     :search
                     ;; TODO textinput is triggering a onChange event even when the value is not changed (arrow keys, command key, etc)
                     (when (not= (:value msg) (om/get-state owner :search-value))
                       (om/set-state! owner :search-value (:value msg))
                       (when-let [channel (om/get-state owner :channel)]
                         (put! channel {:event-type :search-updated
                                        :value (:value msg)}))))
                   (recur)))))

    om/IDidUpdate
    (did-update [this prev-props prev-state]
      (when-not (= (om/get-state owner :opened)
                   (:opened prev-state))
        (when (false? (om/get-state owner :opened))
          (when (om/get-state owner :channel)
            (put! (om/get-state owner :channel) {:event-type :closed})))))


    om/IRenderState
    (render-state
      [this {:keys [id path opened disabled label placeholder as-link? icon onBlur onClick default options loading-more? loading? local-channel label-key]  :as state}]
      (html
        [:div (merge {:class (str "btn-group " (when opened "open ") (or class-name "btn-group-xs"))
                      :onKeyDown (fn [event] (handle-keydown owner event options))}
                     (when id
                       {:id id}))

         (om/build open-button target {:state {:channel local-channel
                                               :path path
                                               :disabled disabled
                                               :label label
                                               :label-key label-key
                                               :placeholder placeholder
                                               :as-link? as-link?
                                               :options options
                                               :icon icon
                                               :onClick onClick
                                               :default default}})

         (when opened
           (om/build popover-container
                     nil
                     {:state {:content-fn (fn []
                                            (let [options (->> options
                                                               (th/if->> loading-more?
                                                                         (#(conj % [:custom-html
                                                                                    [:div.loading-dots]]))
                                                                         (#(concat % [[:divider ""]
                                                                                      [:custom-html
                                                                                       [:li [:a {:class "btn btn-link btn-sm"
                                                                                                 :onClick (fn []
                                                                                                            (when (om/get-state owner :channel)
                                                                                                              (put! (om/get-state owner :channel) {:event-type :load-more})))}
                                                                                             "Load More"]]]]))))]
                                              (om/build body target {:state {:options options
                                                                             :path (:path state)
                                                                             :channel local-channel
                                                                             :loading? loading?
                                                                             :typing-timeout 300
                                                                             :class-name (:class-name state)
                                                                             :onChange (:onChange state)
                                                                             :onClick (:onClick state)
                                                                             :loading-view (:loading-view state)
                                                                             :searchable? true
                                                                             :divider-class divider-class
                                                                             :list-class list-class}})))
                              :prefered-side :bottom}
                      :opts {:align 0
                             :mouse-down #(do
                                            (om/update-state! owner (fn [s] (merge s {:opened false
                                                                                      :search-value nil
                                                                                      :selected nil
                                                                                      :selected-index -1})))
                                            (when onBlur
                                              (onBlur)))
                             :popover-class "combobox"}}))]))))
