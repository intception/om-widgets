(ns om-widgets.editable-list-view
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [om-widgets.utils :as utils ]
            [om-widgets.textinput :refer [textinput]]
            [cljs.core.async :refer [put! chan <!]])
  (:import [goog.ui IdGenerator ]))

(defn- list-group-item [item owner {:keys [index]}]
  (reify
    om/IRenderState
    (render-state [this {:keys [delete disabled input-format]}]

      (dom/li #js { :className "list-group-item" }
        (dom/span nil (condp =  input-format
                        "date" (utils/get-utc-formatted-date item)
                        (str item)))
        (dom/button #js {:className "btn btn-danger pull-right btn-xs"
                         :disabled disabled
                         :onClick (fn [e]
                                    (put! delete index )
                                    nil)}
          (dom/span #js {:dangerouslySetInnerHTML #js {:__html "&nbsp;"}})
          (dom/span #js { :className "glyphicon glyphicon-remove"}))))))

(defn- editable-list [app owner]
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
                                items-react-prefix ] :as state}]


      (apply dom/ul #js { :className "list-group" }
        (dom/div #js {:className "input-group"}

          ;; Text Input
          (textinput owner
                     :input-value
                     { :input-format input-format
                       :id id
                       :disabled disabled
                       :className class-name
                       :input-class "form-control list-group-item"
                       :onChange (fn [e]
                                  (om/set-state! owner :field-is-valid
                                                 (condp = input-format
                                                   "date" (and (instance? js/Date e)
                                                               (field-validation-fn e))
                                                   (field-validation-fn e))))})

          ;; Add Button
          (dom/span #js {:className "input-group-btn"}
            (dom/button #js {:className "btn btn-success"
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
                 (dom/span #js {:dangerouslySetInnerHTML #js {:__html "&nbsp;"}})
                 (dom/span #js {:className "glyphicon glyphicon-plus"}) btn-text)))

        (if (empty? (get-in app [path]))
          (seq [(dom/li #js {:className "list-group-item"})])
          (mapv (fn [item i]
            (om/build list-group-item item {:init-state {:delete delete
                                                         :disabled disabled
                                                         :input-format input-format}
                                            :opts {:index i}
                                            :react-key (str items-react-prefix "-" i)}))
                (get-in app [path]) (range)))))))


;; ---------------------------------------------------------------------
;; Public

(defn editable-list-view
  [app path {:keys [class-name id input-format disabled field-validation-fn btn-text]
             :or {field-validation-fn (fn [d] true)}}]
  (om/build editable-list app {:state {:path path
                                       :class-name class-name
                                       :id id
                                       :btn-text btn-text
                                       :disabled disabled
                                       :field-validation-fn field-validation-fn
                                       :input-format input-format}}))
