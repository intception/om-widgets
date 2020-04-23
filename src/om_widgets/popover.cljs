(ns om-widgets.popover
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:import [goog.events EventType])
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs.core.async :refer [put! chan <! alts! timeout close!]]
            [sablono.core :refer-macros [html]]
            [dommy.core :as dommy :refer-macros [sel sel1]]
            [goog.events :as events]
            [om-widgets.utils :as u]))


(defn- get-window-scroll!
  "Get js/window scroll {:scroll-x :scroll-y}
  Note: fn with side-effects"
  []
  (cond (not (nil? (.-pageYOffset js/window)))
        {:scroll-x (.-pageXOffset js/window)
         :scroll-y (.-pageYOffset js/window)}

        (not (nil? (.-scrollTop js/document.body)))
        {:scroll-x (.-scrollLeft js/document)
         :scroll-y (.-scrollTop js/document)}

        (not (nil? (.-scrollTop js/document.documentElement)))
        {:scroll-x (.-scrollLeft js/document.documentElement)
         :scroll-y (.-scrollTop js/document.documentElement)}))

(defn window-size []
  (merge {:scroll-x 0
          :scroll-y 0}
         (u/get-window-boundaries!)
         (get-window-scroll!)))

(defn client-rects
  [el]
  (let [rs (.getClientRects el)]
    (if (not (nil? (.-length rs)))
      (map (fn [i]
             (let [r (.item rs i)]
               {:top (.-top r)
                :bottom (.-bottom r)
                :left (.-left r)
                :right (.-right r)
                :width (.-width r)
                :height (.-height r)})) (range (.-length rs)))
      [{:top (.-top rs)
        :bottom (.-bottom rs)
        :left (.-left rs)
        :right (.-right rs)
        :width (.-width rs)
        :height (.-height rs)}])))


(defn app-root []
  (or (sel1 "div [data-reactid]")
      (sel1 "div [data-reactroot]")))

(defn popover-overlay
  [_ owner]
  (reify
    om/IDidMount
    (did-mount [this]
      (let [node (om.core/get-node owner)
            parent (dommy/remove! node)]
        (dommy/append! (app-root) node)
        (om/update-state! owner (fn [st]
                                  (merge st {:node node
                                             :parent parent})))))
    om/IWillUnmount
    (will-unmount [this]
      (dommy/remove! (om/get-state owner :node))
      (dommy/append! (om/get-state owner :parent) (om/get-state owner :node)))

    om/IRenderState
    (render-state [this {:keys [mouse-down]}]
      (html
        [:div {:class "om-widgets-popover-overlay"
               :onMouseDown #(when mouse-down
                              (mouse-down)
                              nil)}]))))

(defn arrow-offset-align [vl0 vl1 align]
  (+ vl0 (* align (- vl1 vl0))))


(defmulti select-side (fn [prefered-side _ _ _] prefered-side))

(defmethod select-side :top
  [_ target-pos bounding-rect wz]
  (cond
    (>= (- (:top target-pos)
           (+ (:height bounding-rect) 20))
        (:scroll-y wz)) :top

    (<= (+ (:bottom target-pos) (:height bounding-rect))
        (+ (:scroll-y wz) (- (:height wz) 20))) :bottom
    (<= (+ (:right  target-pos) (:width  bounding-rect))
        (+ (:scroll-x wz) (- (:width  wz) 20))) :right
    (>= (- (:left target-pos) (+ (:width bounding-rect) 20))
        (:scroll-x wz)) :left
    :else :center))

(defmethod select-side :bottom
  [_ target-pos bounding-rect wz]
  (cond
    (<= (+ (:bottom target-pos) (:height bounding-rect))
        (+ (:scroll-y wz) (- (:height wz) 20))) :bottom
    (>= (- (:top target-pos)
           (+ (:height bounding-rect) 20))
        (:scroll-y wz)) :top
    (<= (+ (:right  target-pos) (:width  bounding-rect))
        (+ (:scroll-x wz) (- (:width  wz) 20))) :right
    (>= (- (:left target-pos) (+ (:width bounding-rect) 20))
        (:scroll-x wz)) :left
    :else :center))

(defmethod select-side :right
  [_ target-pos bounding-rect wz]
  (cond
    (<= (+ (:right  target-pos) (:width  bounding-rect))
        (+ (:scroll-x wz) (- (:width  wz) 20))) :right
    (>= (- (:left target-pos) (+ (:width bounding-rect) 20))
        (:scroll-x wz)) :left
    (>= (- (:top target-pos)
           (+ (:height bounding-rect) 20))
        (:scroll-y wz)) :top
    (<= (+ (:bottom target-pos) (:height bounding-rect))
        (+ (:scroll-y wz) (- (:height wz) 20))) :bottom
    :else :center))

(defmethod select-side :left
  [_ target-pos bounding-rect wz]
  (cond (>= (- (:left target-pos) (+ (:width bounding-rect) 20))
            (:scroll-x wz)) :left
        (<= (+ (:right  target-pos) (:width  bounding-rect))
            (+ (:scroll-x wz) (- (:width  wz) 20))) :right
        (>= (- (:top target-pos)
               (+ (:height bounding-rect) 20))
            (:scroll-y wz)) :top
        (<= (+ (:bottom target-pos) (:height bounding-rect))
            (+ (:scroll-y wz) (- (:height wz) 20))) :bottom
        :else :center))


(defn popover-container
  [_ owner opts]
  (let [update-position #(put! (om/get-state owner :channel) :update)]
    (reify
      om/IInitState
      (init-state [this]
        {:channel (chan)
         :side :bottom
         :has-arrow (:has-arrow opts)})

      om/IDidMount
      (did-mount [this]
        (let [node (om.core/get-node owner)
              align (or (:align opts) 0.5)
              coordinates (om/get-state owner :coordinates)
              parent (dommy/remove! node)
              target (if (:for opts)
                       (or (sel1 (keyword (str "#" (:for opts)))) parent)
                       parent)
              arrow (sel1 node ".arrow")]
          (dommy/append! (app-root) node)
          (om/update-state! owner (fn [st]
                                    (merge st {:node node
                                               :parent parent})))
          (go-loop []
            (let [message (<! (om/get-state owner :channel))]
              (when-not (= message :quit)
                (let [ofs-max (fn [o m]
                                (if (<  o 0)
                                  (max o (- m))
                                  (min o m)))
                      apply-zoom-factor (fn [rect zoomed-container]
                                          (if-let [zn (.getElementById js/document zoomed-container)]
                                            (let [zoom (.-zoom (.getComputedStyle js/window zn ""))]
                                              (merge rect {:top (* (:top rect) zoom)
                                                           :left (* (:left rect) zoom)
                                                           :bottom (* (:bottom rect) zoom)
                                                           :right (* (:right rect) zoom)
                                                           :width (* (:width rect) zoom)
                                                           :height (* (:height rect) zoom)}))
                                            rect))
                      wz (window-size)
                      trect (-> (if coordinates
                                  {:top (:y coordinates)
                                   :bottom (+ (:y coordinates) 1)
                                   :left (:x coordinates)
                                   :right (+ (:x coordinates) 1)
                                   :width 1
                                   :height 1}
                                  (first (client-rects target)))
                                (cond-> (:zoomed-container opts)
                                        (apply-zoom-factor (:zoomed-container opts))))
                      target-pos (merge trect
                                        {:top (+ (:top trect) (:scroll-y wz))
                                         :bottom (+ (:bottom trect) (:scroll-y wz))
                                         :left (+ (:left trect) (:scroll-x wz))
                                         :right (+ (:right trect) (:scroll-x wz))})
                      bounding-rect (dommy/bounding-client-rect node)
                      side (select-side (om/get-state owner :prefered-side) target-pos bounding-rect wz)
                      y (condp = side
                          :top    (- (:top target-pos)
                                     (:height bounding-rect))
                          :bottom (:bottom target-pos)
                          :right  (- (+ (:top target-pos) (* (:height target-pos) align))
                                     (* (:height bounding-rect) align))
                          :left   (- (+ (:top target-pos) (* (:height target-pos) align))
                                     (* (:height bounding-rect) align))
                          :center (- (+ (/ (:height wz) 2)
                                        (:scroll-y wz))
                                     (/ (:height bounding-rect) 2)))

                      x (condp = side
                          :top    (- (+ (:left target-pos) (* (:width target-pos) align))
                                     (* (:width bounding-rect) align))
                          :bottom (- (+ (:left target-pos) (* (:width target-pos) align))
                                     (* (:width bounding-rect) align))
                          :right  (:right target-pos)
                          :left   (- (:left target-pos) (:width bounding-rect))
                          :center (- (+ (/ (:width wz) 2)
                                        (:scroll-x wz))
                                     (/ (:width bounding-rect) 2)))

                      offset-left (ofs-max (if (contains? #{:top :bottom} side)
                                             (cond (< x (:scroll-x wz)) (- (:scroll-x wz) x)
                                                   (> (+ x (:width bounding-rect)) (+ (:width wz) (:scroll-x wz))) (-  (+ (:width wz) (:scroll-x wz)) (+ x (:width bounding-rect)))
                                                   :else 0)
                                             0)
                                           (- (/ (:width bounding-rect) 2) 20))
                      arrow-left (* 100 (- align (/ (- offset-left (arrow-offset-align 14 -14 align)) (:width bounding-rect))))
                      offset-top (ofs-max (if (contains? #{:left :right} side)
                                            (cond (< y (:scroll-y wz)) (- (:scroll-y wz) y)
                                                  (> (+ y (:height bounding-rect)) (+ (:height wz) (:scroll-y wz))) (-  (+ (:height wz) (:scroll-y wz)) (+ y (:height bounding-rect)))
                                                  :else 0)
                                            0)
                                          (- (/ (:height bounding-rect) 2) 20))
                      has-arrow (if-not (= side :center)
                                  (:has-arrow opts)
                                  false)
                      arrow-top (* 100 (- align (/ (- offset-top (arrow-offset-align 14 -14 align))(:height bounding-rect))))]
                  (om/set-state! owner :has-arrow has-arrow)
                  (when-not (= (om/get-state owner :side) side)
                    (om/set-state! owner :side side))
                  (dommy/set-px! node :top (+ y offset-top) :left (+ x offset-left))
                  (when arrow
                    (if (contains? #{:top :bottom} side)
                      (dommy/set-style! arrow :left (str arrow-left "%"))
                      (dommy/set-style! arrow :top (str arrow-top "%"))))
                  (recur)))))
          (events/listen js/window EventType.RESIZE update-position)
          (put! (om/get-state owner :channel) :update)))

      om/IWillUnmount
      (will-unmount [this]
        (dommy/remove! (om/get-state owner :node))
        (dommy/append! (om/get-state owner :parent) (om/get-state owner :node))
        (events/unlisten js/window EventType.RESIZE update-position)
        (put! (om/get-state owner :channel) :quit))

      om/IRenderState
      (render-state [this {:keys [label side has-arrow content-fn mouse-down] :as state}]

        (html
          [:div {:class (str  "om-widgets-popover " (name side) " " (:popover-class opts))}
           (when (:mouse-down opts)
             (om/build popover-overlay nil {:state {:mouse-down (:mouse-down opts)}}))
           [:span {:class (when has-arrow "arrow")}]

           [:div {:class "popover-container"}
            (content-fn (:close-fn opts))]])))))

(defn popover-component
  [_ owner opts]
  (reify
    om/IRenderState
    (render-state [this {:keys [visible-content-fn popup-content-fn visible prefered-side channel coordinates] :as state}]
      (html
        [:div {:class (:launcher-class-name opts)}
         (when visible
           (dom/div #js {:style #js {:position "absolute" :display "inline"}}
                    (om/build popover-container nil {:init-state {:channel channel}
                                                     :state {:content-fn popup-content-fn
                                                             :prefered-side prefered-side
                                                             :coordinates coordinates}
                                                     :opts {:for (:for opts)
                                                            :align (:align opts)
                                                            :has-arrow (:has-arrow opts)
                                                            :mouse-down #(om/set-state! owner :visible false)
                                                            :popover-class (:popover-class opts)
                                                            :zoomed-container (:zoomed-container opts)
                                                            :close-fn #(go
                                                                        (<! (timeout 10))
                                                                        (om/set-state! owner :visible false))}})))
         (visible-content-fn (fn []
                               (om/update-state! owner (fn [st]
                                                         (merge st {:visible true})))))]))))


(defn- labeled-popover-component
  [_ owner opts]
  (reify
    om/IInitState
    (init-state [this]
      {:visible false})

    om/IRenderState
    (render-state [this {:keys [label id disabled class-name visible body prefered-side channel coordinates]}]
      (html
        [:div {:class (:launcher-class-name opts)}
         [:a {:class class-name
              :id id
              :disabled disabled
              :onClick #(do
                         (om/set-state! owner :visible true)
                         nil)}
          label]
         (when visible
           (om/build popover-container nil {:state {:content-fn body
                                                    :prefered-side prefered-side
                                                    :coordinates coordinates
                                                    :channel channel}
                                            :opts {:align (:align opts)
                                                   :has-arrow (:has-arrow opts)
                                                   :mouse-down #(om/set-state! owner :visible false)
                                                   :zoomed-container (:zoomed-container opts)
                                                   :popover-class (:popover-class opts)
                                                   :close-fn #(go
                                                               (<! (timeout 10))
                                                               (om/set-state! owner :visible false))}}))]))))


;; ---------------------------------------------------------------------
;; Public

(defn popover
  "Display a pop over screen window
  When front-face is a string build a <a tag
  When front-face is a function must return a om dom/ component.
  class-name id and disabled works only when front-face is a string
  prefered-side :top, :bottom, :right, :left default = :bottom
  "
  [front-face popup-body {:keys [class-name
                                 id
                                 disabled
                                 for
                                 prefered-side
                                 has-arrow
                                 popover-class
                                 launcher-class-name
                                 zoomed-container
                                 channel
                                 align
                                 visible
                                 coordinates]
                          :or {class-name "om-widgets-popover-button"
                               prefered-side :bottom
                               popover-class ""
                               launcher-class-name "om-widgets-popover-launcher"
                               has-arrow true
                               visible false
                               channel (chan)
                               align 0.5}}]
  (cond
    (fn? front-face)
    (om/build popover-component nil {:init-state {:visible visible}
                                     :state {:visible-content-fn front-face
                                             :popup-content-fn popup-body
                                             :prefered-side prefered-side
                                             :channel channel
                                             :coordinates coordinates}
                                     :opts {:for for
                                            :has-arrow has-arrow
                                            :zoomed-container zoomed-container

                                            :popover-class popover-class
                                            :launcher-class-name launcher-class-name
                                            :align align}})
    :else
    (om/build labeled-popover-component nil {:init-state {:visible visible}
                                             :state {:label front-face
                                                     :id (or id front-face)
                                                     :disabled disabled
                                                     :class-name class-name
                                                     :prefered-side prefered-side
                                                     :body popup-body
                                                     :channel channel
                                                     :coordinates coordinates}
                                             :opts {:align align
                                                    :popover-class popover-class
                                                    :zoomed-container zoomed-container
                                                    :launcher-class-name launcher-class-name
                                                    :has-arrow has-arrow}})))
