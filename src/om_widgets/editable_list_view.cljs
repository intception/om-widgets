(ns om-widgets.editable-list-view
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [schema.core :as s :include-macros true]
            [om-widgets.utils :as utils]
            [sablono.core :as html :refer-macros [html]]
            [om-widgets.textinput :refer [textinput]]
            [cljs.core.async :refer [put! chan <!]])
  (:import [goog.ui IdGenerator ]))


(defn- list-group-item
  [item owner {:keys [index btn-remove-class btn-remove-icon-class]}]
  (reify
    om/IRenderState
    (render-state [_ {:keys [delete disabled input-format]}]
      (html
        [:li.list-group-item
         [:span (condp =  input-format
                  "date" (utils/get-utc-formatted-date item)
                  (str item))]
         [:button {:class (or btn-remove-class "btn btn-danger pull-right btn-xs")
                   :disabled disabled
                   :onClick (fn [e]
                              (put! delete index)
                              nil)}
          (when (not btn-remove-icon-class)
            [:span {:dangerouslySetInnerHTML #js {:__html "&nbsp;"}}])
          [:span {:className (or btn-remove-icon-class "glyphicon glyphicon-remove")}]]]))))

(defn- editable-list
  [app owner]
  (reify
    om/IInitState
    (init-state [_]
      {:delete (chan)
       :field-is-valid false
       :input-value ""
       :items-react-prefix (.getNextUniqueId (.getInstance IdGenerator))})

    om/IWillMount
    (will-mount [_]
      (let [delete (om/get-state owner :delete)
            path (om/get-state owner :path)]
        (go (loop []
              (let [index (<! delete)]
                (om/update! app [path] (utils/exclude-item-at-pos (vec (get-in @app [path])) index))
                (recur))))))

    om/IRenderState
    (render-state [this {:keys [delete path input-format id class-name
                                field-validation-fn field-is-valid disabled btn-text
                                input-class-name input-placeholder input-size
                                items-react-prefix] :as state}]
      (html
        (utils/make-childs
          [:ul {:class ["list-group" (when class-name class-name)]}
           [:li {:class "list-unstyled"}
            [:div.input-group
             (textinput owner
                        :input-value
                        (-> {:id id
                             :disabled disabled
                             :input-class (str (or input-class-name "form-control list-group-item ")
                                               (when input-size (name input-size)))
                             :typing-timeout 100
                             :onChange (fn [e]
                                         (om/set-state! owner :field-is-valid
                                                        (condp = input-format
                                                          "date" (and (instance? js/Date e)
                                                                      (field-validation-fn e))
                                                          (when field-validation-fn
                                                            (field-validation-fn e)))))}
                            (merge (when input-placeholder
                                     (merge {:placeholder input-placeholder})))
                            (merge (when (seq input-format)
                                     (merge {:input-format input-format})))))
             [:div.input-group-btn
              [:button {:class (or (:btn-add-class state) "btn btn-success")
                        :type "button"
                        :disabled (or disabled (not field-is-valid))
                        :onClick (fn [e]
                                   (utils/om-update! (om/get-props owner)
                                                     [path]
                                                     (fn [w]
                                                       ;; TODO if the path does not exist
                                                       ;; conj of nil return a list
                                                       (conj w (om/get-state owner :input-value))))
                                   (om/set-state! owner :input-value "")
                                   (om/set-state! owner :field-is-valid nil))}
               (when (not (:btn-add-icon-class state))
                 [:span {:dangerouslySetInnerHTML #js {:__html "&nbsp;"}}])
               [:span {:class (or (:btn-add-icon-class state) "glyphicon glyphicon-plus")}]
               (when btn-text btn-text)]]]]]

          (if (empty? (get-in app [path]))
            (seq [(dom/li #js {:className "list-group-item"})])
            (mapv (fn [item i]
                    (om/build list-group-item item {:init-state {:delete delete
                                                                 :disabled disabled
                                                                 :input-format input-format}
                                                    :opts {:index i
                                                           :btn-remove-class (:btn-remove-class state)
                                                           :btn-remove-icon-class (:btn-remove-icon-class state)}
                                                    :react-key (str items-react-prefix "-" i)}))
                  (get-in app [path]) (range))))))))


;; ---------------------------------------------------------------------
;; Schema
(def ListSchema
  {(s/optional-key :class-name) s/Str
   (s/optional-key :input-class-name) s/Str
   (s/optional-key :input-size) (s/enum :input-sm :input-xs :input-md :input-lg)
   (s/optional-key :input-format) s/Str
   (s/optional-key :input-placeholder) s/Str
   (s/optional-key :id) s/Str
   (s/optional-key :disabled) s/Bool
   (s/optional-key :field-validation-fn) (s/pred fn?)
   (s/optional-key :btn-text) s/Str
   (s/optional-key :btn-add-class) s/Str
   (s/optional-key :btn-add-icon-class) s/Str
   (s/optional-key :btn-remove-class) s/Str
   (s/optional-key :btn-remove-icon-class) s/Str})

;; ---------------------------------------------------------------------
;; Public
(defn editable-list-view
  [app path opts]
  (when opts (s/validate ListSchema opts))
  (om/build editable-list app {:state (-> (or opts {})
                                          (merge {:path path
                                                  :field-validation-fn (or (:field-validation-fn opts)
                                                                           (fn [d] true))}))}))
