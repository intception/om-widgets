(ns om-widgets.popover
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:import [goog.events EventType])
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs.core.async :refer [put! chan <! alts! timeout close!]]
            [sablono.core :as html :refer-macros [html]]
            [dommy.core :as dommy :refer-macros [sel sel1]]
            [goog.events :as events]))

(defn window-size []
  (merge {:scroll-x 0
          :scroll-y 0}
         (cond (not= nil (type (.-innerWidth js/window))) {:width (.-innerWidth js/window)
                                                           :height (.-innerHeight js/window)}

               (and (not= nil (type (.-documentElement js/document)))
                    (.-clientWidth js/document.documentElement)
                    (not= 0 (.-documentElement.clientWidth js/document))) {:width (.-clientWidth js/document.documentElement)
                                                                           :height (.-clientHeight js/document.documentElement)}

               :else {:width (.-clientWidth (aget (.getElementsByTagName js/document "body") 0))
                      :height (.-clientHeight (aget (.getElementsByTagName js/document "body") 0))})
         (cond (not (nil? (.-pageYOffset js/window))) {:scroll-x (.-pageXOffset js/window)
                                                       :scroll-y (.-pageYOffset js/window)}
               (not (nil? (.-scrollTop js/document.body))) {:scroll-x (.-scrollLeft js/document)
                                                            :scroll-y (.-scrollTop js/document)}
               (not (nil? (.-scrollTop js/document.documentElement))) {:scroll-x (.-scrollLeft js/document.documentElement)
                                                                       :scroll-y (.-scrollTop js/document.documentElement)})))
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
  (sel1 "div [data-reactid=\".0\"]"))

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
              :onMouseDown #(when mouse-down (mouse-down) false)}]))))
(defn arrow-offset-align [vl0 vl1 align]
  (+ vl0 (* align (- vl1 vl0))))

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
                      wz (window-size)
                      trect (first (client-rects target));;(dommy/bounding-client-rect target)
                      target-pos (merge trect
                                        {:top (+ (:top trect) (:scroll-y wz))
                                        :bottom (+ (:bottom trect) (:scroll-y wz))
                                        :left (+ (:left trect) (:scroll-x wz))
                                        :right (+ (:right trect) (:scroll-x wz))})
                      bounding-rect (dommy/bounding-client-rect node)
                      side (condp =  (om/get-state owner :prefered-side)
                            :top  (cond
                                    (>= (- (:top target-pos)
                                           (+ (:height bounding-rect) 20))
                                        (:scroll-y wz)) :top

                                    (<= (+ (:bottom target-pos) (:height bounding-rect))
                                        (+ (:scroll-y wz) (- (:height wz) 20))) :bottom
                                    (<= (+ (:right  target-pos) (:width  bounding-rect))
                                        (+ (:scroll-x wz) (- (:width  wz) 20))) :right
                                    (>= (- (:left target-pos) (+ (:width bounding-rect) 20))
                                        (:scroll-x wz)) :left
                                    :else :center)

                            :bottom (cond
                                      (<= (+ (:bottom target-pos) (:height bounding-rect))
                                            (+ (:scroll-y wz) (- (:height wz) 20))) :bottom
                                      (>= (- (:top target-pos)
                                             (+ (:height bounding-rect) 20))
                                          (:scroll-y wz)) :top
                                      (<= (+ (:right  target-pos) (:width  bounding-rect))
                                          (+ (:scroll-x wz) (- (:width  wz) 20))) :right
                                      (>= (- (:left target-pos) (+ (:width bounding-rect) 20))
                                          (:scroll-x wz)) :left
                                      :else :center)

                            :right  (cond
                                      (<= (+ (:right  target-pos) (:width  bounding-rect))
                                            (+ (:scroll-x wz) (- (:width  wz) 20))) :right
                                      (>= (- (:left target-pos) (+ (:width bounding-rect) 20))
                                          (:scroll-x wz)) :left
                                      (>= (- (:top target-pos)
                                             (+ (:height bounding-rect) 20))
                                          (:scroll-y wz)) :top
                                      (<= (+ (:bottom target-pos) (:height bounding-rect))
                                          (+ (:scroll-y wz) (- (:height wz) 20))) :bottom
                                      :else :center)
                            :left   (cond (>= (- (:left target-pos) (+ (:width bounding-rect) 20))
                                            (:scroll-x wz)) :left
                                          (<= (+ (:right  target-pos) (:width  bounding-rect))
                                              (+ (:scroll-x wz) (- (:width  wz) 20))) :right
                                          (>= (- (:top target-pos)
                                                 (+ (:height bounding-rect) 20))
                                              (:scroll-y wz)) :top
                                          (<= (+ (:bottom target-pos) (:height bounding-rect))
                                              (+ (:scroll-y wz) (- (:height wz) 20))) :bottom
                                          :else :center))
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

                      offset-left   (ofs-max (if (contains? #{:top :bottom} side)
                                            (cond (< x (:scroll-x wz)) (- (:scroll-x wz) x)
                                                  (> (+ x (:width bounding-rect)) (+ (:width wz) (:scroll-x wz))) (-  (+ (:width wz) (:scroll-x wz)) (+ x (:width bounding-rect)))
                                                  :else 0)
                                            0)
                                           (- (/ (:width bounding-rect) 2) 20))
                      arrow-left (* 100 (- align (/ (- offset-left (arrow-offset-align 14 -14 align)) (:width bounding-rect))))
                      offset-top    (ofs-max (if (contains? #{:left :right} side)
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
                (put! (om/get-state owner :channel) :update)
          ))
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
    (render-state [this {:keys [visible-content-fn popup-content-fn visible prefered-side channel] :as state}]
      (html
       [:div {:class "om-widgets-popover-launcher"}
        (when visible
          (dom/div #js {:style #js {:position "absolute" :display "inline"}}
                   (om/build popover-container nil {:state {:content-fn popup-content-fn
                                                            :prefered-side prefered-side}
                                                    :opts {:for (:for opts)
                                                           :align (:align opts)
                                                           :has-arrow (:has-arrow opts)
                                                           :mouse-down #(om/set-state! owner :visible false)
                                                           :popover-class (:popover-class opts)
                                                           :close-fn #(go
                                                                        (<! (timeout 10))
                                                                        (om/set-state! owner :visible false))}})))
        (visible-content-fn (fn []
                              (om/update-state! owner (fn [st]
                                                        (merge st {:visible true})))))]))))



(defn- labeled-popover-component [_ owner opts]
  (reify
    om/IDisplayName
    (display-name [_] "PopOver")

    om/IInitState
    (init-state [this]
      {:visible false})
    om/IRenderState
    (render-state [this {:keys [label id disabled class-name visible body prefered-side]}]
      (dom/div #js {:className "om-widgets-popover-launcher"}
               (dom/a #js {:className class-name
                           :href "#"
                           :type "button"
                           :id id
                           :disabled disabled
                           :onClick #(do
                                       (om/set-state! owner :visible true)
                                       false)}
                      label
                      (when visible (om/build popover-container nil {:state {:content-fn body :prefered-side prefered-side}
                                                                     :opts {:align (:align opts)
                                                                            :has-arrow (:has-arrow opts)
                                                                            :mouse-down #(om/set-state! owner :visible false)
                                                                            :popover-class (:popover-class opts)
                                                                            :close-fn #(go
                                                                                         (<! (timeout 10))
                                                                                         (om/set-state! owner :visible false))}})))))))


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
                                 align]
                          :or {class-name "om-widgets-popover-button"
                               prefered-side :bottom
                               popover-class ""
                               has-arrow true
                               align 0.5}}]
  (cond
    (fn? front-face)
    (om/build popover-component nil {:state {:visible-content-fn front-face
                                             :popup-content-fn popup-body
                                             :prefered-side prefered-side}
                                     :opts {:for for
                                            :has-arrow has-arrow
                                            :popover-class popover-class
                                            :align align}})
    :else
    (om/build labeled-popover-component nil {:state {:label front-face
                                                     :id (or id front-face)
                                                     :disabled disabled
                                                     :class-name class-name

                                                     :prefered-side prefered-side
                                                     :body popup-body}
                                             :opts {:align align
                                                    :popover-class popover-class
                                                    :has-arrow has-arrow}})))

