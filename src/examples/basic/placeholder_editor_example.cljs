(ns examples.basic.placeholder-editor-example
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs.core.async :refer [put! chan <! alts! timeout close!]]
            [dommy.core :as dommy :refer-macros [sel sel1]]
            [sablono.core :as html :refer-macros [html]]
            [om-widgets.core :as w]))
; externas
; utils

(defn make-childs
  "Build all alternative for sablono"
  [tag childs]
  (vec (concat (if (vector? tag)
                 tag
                 [tag]) childs)))


(defn client-rect
  [el]
  (let [rect  (.getBoundingClientRect el)]
        {:left (.-left rect)
         :width (.-width rect)
         :top (.-top rect)
         :height (.-height rect)}))

(defn- point-in-rect?
  [x y rect]
  (and (and (>= x (:left rect))
            (<= x (+ (:left rect) (:width rect))))
       (and (>= y (:top rect))
            (<= y (+ (:top rect) (:height rect))))))

(defn- image-uri
  [image]
  (str (:id image) ".png"))

(defn text-overprinted
  [text-data owner]
  (reify
    om/IRenderState
    (render-state [this state]
      (html
        [:div {:style #js {:font-family (:font-family text-data)
                           :font-size (str (:font-size text-data) "px")
                           :font-style (:font-style text-data)
                           :font-weight (:font-weight text-data)
                           :color (:color text-data)}}
         (:text text-data)]))))

(defn sdref [data]
  (if (om/rendering?)
    data
    (if (satisfies? IDeref data)
      @data
      data)))

(defn box-type [overprint-data]
  (condp = (:type (sdref overprint-data))
    :text :box))

(defmulti get-handlers box-type)
(defmethod get-handlers :box
  [overprint-data]
  (let [x (get (sdref overprint-data) :left)
        y (get (sdref overprint-data) :top)
        hh (+ x (/ (get (sdref overprint-data) :width) 2))
        hv (+ y (/ (get (sdref overprint-data) :height) 2))
        r (+ x (get (sdref overprint-data) :width))
        b (+ y (get (sdref overprint-data) :height))]
    [{:type :nwse-resize-top :left (- x 4) :top (- y 4) :width 9 :height 9 :cursor "nwse-resize"}
     {:type :ns-resize-top :left (- hh 4) :top (- y 4) :width 9 :height 9 :cursor "ns-resize"}
     {:type :nesw-resize-top :left (- r 4) :top (- y 4) :width 9 :height 9 :cursor "nesw-resize"}
     {:type :e-resize-right :left (- r 4) :top (- hv 4) :width 9 :height 9 :cursor "e-resize"}
     {:type :nwse-resize-bottom :left (- r 4) :top (- b 4) :width 9 :height 9 :cursor "nwse-resize"}
     {:type :ns-resize-bottom :left (- hh 4) :top (- b 4) :width 9 :height 9 :cursor "ns-resize"}
     {:type :nesw-resize-bottom :left (- x 4) :top (- b 4) :width 9 :height 9 :cursor "nesw-resize"}
     {:type :w-resize-left :left (- x 4) :top (- hv 4) :width 9 :height 9 :cursor "w-resize"}]))

(defmulti hit-test box-type)
(defmethod hit-test :box
  [overprint-data x y]
  (if (get (sdref overprint-data) :selected)
    (if-let [handler (first (filter (fn [handler]
                                      (point-in-rect? x y handler)) (get-handlers overprint-data)))]
      {:type    :handler
       :handler handler}
      (when (point-in-rect? x y (sdref overprint-data))
        {:type :face}))
    (when (point-in-rect? x y (sdref overprint-data))
      {:type :face})))

(defmulti drag box-type)
(defmethod drag :box
  [overprint-data x y]
  (merge overprint-data {:left x :top y}))

(defmulti drag-handler box-type)
(defmethod drag-handler :box
  [overprint-data handler target x y]
  (let [dx (- x (:left handler))
        dy (- y (:top handler))]
    (merge overprint-data {:left   (condp = (:type handler)
                                     :nwse-resize-top (+ (:left target) dx)
                                     :nesw-resize-bottom (+ (:left target) dx)
                                     :w-resize-left (+ (:left target) dx)
                                     (:left target))
                           :width  (condp = (:type handler)
                                     :nwse-resize-top (+ (:width target) (- dx))
                                     :nesw-resize-top (+ (:width target) dx)
                                     :e-resize-right (+ (:width target) dx)
                                     :nwse-resize-bottom (+ (:width target) dx)
                                     :nesw-resize-bottom (+ (:width target) (- dx))
                                     :w-resize-left (+ (:width target) (- dx))
                                     (:width target))
                           :top    (condp = (:type handler)
                                     :nwse-resize-top (+ (:top target) dy)
                                     :ns-resize-top (+ (:top target) dy)
                                     :nesw-resize-top (+ (:top target) dy)
                                     (:top target))
                           :height (condp = (:type handler)
                                     :nwse-resize-top (+ (:height target) (- dy))
                                     :ns-resize-top (+ (:height target) (- dy))
                                     :nesw-resize-top (+ (:height target) (- dy))
                                     :nwse-resize-bottom (+ (:height target) dy)
                                     :ns-resize-bottom (+ (:height target) dy)
                                     :nesw-resize-bottom (+ (:height target) dy)
                                     (:height target))})))

(defn overprint-box
  [overprint-data owner]
  (reify
    om/IRenderState
    (render-state [this {:keys [content] :as state}]
      (html
        [:div {:class "overprint-box"
               :style #js {:position   "absolute"
                           :overflow   "visible"
                           :background (:background overprint-data)
                           :text-align (:text-align overprint-data)
                           :top        (str (:top overprint-data) "px")
                           :left       (str (:left overprint-data) "px")
                           :width      (str (:width overprint-data) "px")
                           :height     (str (:height overprint-data) "px")}}
         [:div {:style #js {:overflow "hidden" :width "100%" :height "100%"}}
          (content overprint-data)]]))))

(defmulti build-overprint :type)
(defmethod build-overprint :text
  [overprint-data overprints]
  (om/build overprint-box overprint-data {:state {:content #(om/build text-overprinted %)}}))


(defn placeholder-editor
  [banner owner]
  (reify
    om/IInitState
    (init-state [this]
      {:cursor "default"})
    om/IRenderState

    (render-state [this {:keys [placeholder-id abtest-index cursor] :as state}]
      (let [images (:images banner)
            img-hash (into {} (map (fn [img] [(:db/id img) {:id       (:id img)
                                                            :filename (:filename img)}]) images))
            image (get img-hash (get-in banner [:image-map placeholder-id abtest-index :image]))
            placeholder (get (into {} (map #(do [(:id %) %]) (get banner :placeholders))) placeholder-id)
            selected (first (filter :selected (get-in banner [:image-map placeholder-id abtest-index :overprints])))]
        (html
          [:div {:class "placeholder-editor"}
           [:div {:class       "placeholder-editor-frame"
                  :tabIndex    (str 0)
                  :style       #js {:cursor cursor}
                  :onMouseDown (fn [e]
                                 (let [df-bounds (client-rect (sel1 (om/get-node owner) (keyword ".draw-frame")))
                                       x (- (.-clientX e) (:left df-bounds))
                                       y (- (.-clientY e) (:top df-bounds))
                                       selected (first (filter :selected (get-in @(om/get-props owner) [:image-map placeholder-id abtest-index :overprints])))
                                       hit-target (if (and selected (hit-test selected x y))
                                                    selected
                                                    (first (filter (fn [ovp]
                                                                     (hit-test ovp x y))
                                                                   (reverse (get-in @(om/get-props owner) [:image-map placeholder-id abtest-index :overprints])))))
                                       hit-info (when hit-target {:hit    (hit-test hit-target x y)
                                                                  :target hit-target})]
                                   (om/transact! (om/get-props owner)
                                                 [:image-map placeholder-id abtest-index :overprints] (fn [overprints]
                                                                                                        (mapv (fn [o]
                                                                                                                (assoc o :selected (= o (:target (or hit-info {}))))) overprints)))
                                   (when hit-info
                                     (om/update-state! owner (fn [st]
                                                               (merge st {:x        (.-pageX e)
                                                                          :y        (.-pageY e)
                                                                          :hit-info hit-info
                                                                          :cursor   (if (= :handler (get-in hit-info [:hit :type]))
                                                                                      (get-in hit-info [:hit :handler :cursor])
                                                                                      "default")
                                                                          :drag     true}))))
                                   true))
                  :onMouseMove (fn [e]
                                 (if (om/get-state owner :drag)
                                   (let [hit (om/get-state owner [:hit-info :hit])
                                         target (om/get-state owner [:hit-info :target])
                                         dx (- (.-pageX e) (om/get-state owner :x))
                                         dy (- (.-pageY e) (om/get-state owner :y))

                                         cx (condp = (:type hit)
                                              :face (:left target)
                                              :handler (get-in hit [:handler :left]))


                                         cy (condp = (:type hit)
                                              :face (:top target)
                                              :handler (get-in hit [:handler :top]))
                                         index (count (take-while #(not (:selected %)) (get-in @(om/get-props owner) [:image-map placeholder-id abtest-index :overprints])))
                                         selected (get-in @(om/get-props owner) [:image-map placeholder-id abtest-index :overprints index])]
                                     (condp = (:type hit)
                                       :face (om/update! (om/get-props owner) [:image-map placeholder-id abtest-index :overprints index]
                                                         (drag selected (+ cx dx) (+ cy dy)))
                                       :handler (om/update! (om/get-props owner) [:image-map placeholder-id abtest-index :overprints index]
                                                            (drag-handler selected (:handler hit) target (+ cx dx) (+ cy dy))))))
                                 false)
                  :onMouseUp   (fn [e]
                                 (om/update-state! owner #(merge % {:drag false :cursor "default"}))
                                 false)}
            [:div {:class "draw-frame"
                   :style #js {:cursor cursor}}
             [:img {:src    (image-uri image)
                    :width  (str (:width placeholder) "px")
                    :height (str (:height placeholder) "px")}]
             (make-childs [:div]
                          (map build-overprint (get-in banner [:image-map placeholder-id abtest-index :overprints])))]
            (make-childs [:div {:style #js {:position "absolute" :left "0px" :top "0px"}}]
                         (if selected
                           (mapv (fn [h]
                                   [:div {:class "resize-handler"
                                          :style #js {:left   (str (:left h) "px")
                                                      :top    (str (:top h) "px")
                                                      :width  (str (:width h) "px")
                                                      :height (str (:height h) "px")
                                                      :cursor (:cursor h)
                                                      }}]) (get-handlers selected))
                           []))]
           [:div
            (w/combobox banner
                        [:image-map placeholder-id abtest-index :image]
                        {:options (into {} (map (fn [image]
                                                  [(:db/id image) (:filename image)]) (:images banner)))})
            [:button {:onClick (fn [e]
                                 (om/transact! (om/get-props owner) [:image-map placeholder-id abtest-index :overprints]
                                               #(conj (mapv (fn [o] (dissoc o :selected)) %)
                                                      {:type        :text
                                                       :text        "Text"
                                                       :font-family "verdana"
                                                       :font-size   24
                                                       :text-align  "left"
                                                       :font-weight "bold"
                                                       :color       "#000000"
                                                       :background  "#00000000"
                                                       :font-style  "italic"
                                                       :selected    true
                                                       :width       150
                                                       :height      45
                                                       :top         0
                                                       :left        0})))}
             "Add text"]]
           [:button {:onClick  (fn [e]
                                 (let [d (into [] (filter (fn [o] (not (:selected o)))
                                                          (get-in @(om/get-props owner) [:image-map placeholder-id abtest-index :overprints])))]
                                   (om/update! (om/get-props owner)
                                               [:image-map placeholder-id abtest-index :overprints]
                                               d)))
                     :disabled (= nil selected)}
            "Delete"]
           (w/textinput selected :text {:disabled  (= nil selected)
                                        :multiline true})
           (w/combobox selected :font-family {:options  (sorted-map "arial" "Arial"
                                                                    "arial black" "Arial Black"
                                                                    "comic sans ms" "Comic Sans Ms"
                                                                    "courier" "Courier"
                                                                    "courier new" "Courier New"
                                                                    "cursive" "Cursive"
                                                                    "georgia" "Georgia"
                                                                    "impact" "Impact"
                                                                    "palatino" "Palatino"
                                                                    "times new roman" "Times New Roman"
                                                                    "trebuchet ms" "Trebuchet Ms"
                                                                    "verdana" "Verdana")
                                              :disabled (= nil selected)})
           [:button {:disabled (= nil selected)
                     :onClick  (fn [e]
                                 (om/update! selected :font-size (inc (:font-size @selected))))}
            "+"]
           [:button {:disabled (= nil selected)
                     :onClick  (fn [e]
                                 (om/update! selected :font-size (dec (:font-size @selected))))}
            "-"]
           (w/checkbox selected :font-weight {:label           "Bold"
                                              :checked-value   "bold"
                                              :unchecked-value "normal"
                                              :disabled        (= nil selected)})
           (w/radiobutton selected :font-style {:disabled      (= nil selected)
                                                :label         "Normal"
                                                :checked-value "normal"})
           (w/radiobutton selected :font-style {:disabled      (= nil selected)
                                                :label         "Italic"
                                                :checked-value "italic"})
           (w/radiobutton selected :font-style {:disabled      (= nil selected)
                                                :label         "Oblique"
                                                :checked-value "oblique"})

           (w/radiobutton selected :text-align {:disabled      (= nil selected)
                                                :label         "Left"
                                                :checked-value "left"})

           (w/radiobutton selected :text-align {:disabled      (= nil selected)
                                                :label         "Center"
                                                :checked-value "center"})

           (w/radiobutton selected :text-align {:disabled      (= nil selected)
                                                :label         "Right"
                                                :checked-value "right"})

           (w/radiobutton selected :text-align {:disabled      (= nil selected)
                                                :label         "Justify"
                                                :checked-value "justify"})
           [:input {:id       "front-colorpicker"
                    :type     "color"
                    :value    (:color selected)
                    :disabled (= nil selected)
                    :onChange (fn [e]
                                (om/update! selected :color (.-value (sel1 (om/get-node owner) :#front-colorpicker))))}]
           [:input {:id       "back-colorpicker"
                    :type     "color"
                    :disabled (= nil selected)
                    :value    (:background selected)
                    :onChange (fn [e]
                                (om/update! selected :background (.-value (sel1 (om/get-node owner) :#back-colorpicker))))}]
           [:a {:disabled (= nil selected)
                :onClick  (fn [e]
                            (om/update! selected :background "transparent"))}
            "Reset background"]])))))

(defn placeholder-editor-example
  [app owner]
  (reify
    om/IRenderState
    (render-state [this state]
      (html
        [:div
         (om/build placeholder-editor (get-in app [:banner]) {:init-state {:placeholder-id 17592186045518 :abtest-index 1 }})]))))

