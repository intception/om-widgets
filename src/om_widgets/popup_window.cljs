(ns om-widgets.popup-window
  (:require-macros [dommy.macros :refer [sel sel1]]
                   [cljs.core.async.macros :refer [go go-loop]])
  (:require [om.core :as om :include-macros true]
     [om.dom :as dom :include-macros true]
     [cljs.core.async :refer [put! chan <! alts! timeout close!]]
     [sablono.core :as html :refer-macros [html]]
     [dommy.core :as dommy ]))

(defn window-size []
  (merge {:scroll-x 0
          :scroll-y 0}
        (cond (not= nil (type (.-innerWidth js/window))){:width (.-innerWidth js/window)
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
(defn app-root []
  (sel1 "div [data-reactid=\".0\"]"))

(defn popup-window-overlay
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
    (will-unmount[this]
       (dommy/remove! (om/get-state owner :node))
       (dommy/append! (om/get-state owner :parent) (om/get-state owner :node)))

    om/IRenderState
    (render-state [this {:keys [mouse-down]}]
      (html
        [:div {:class "om-widgets-popup-window-overlay"
               :onMouseDown #(when mouse-down (mouse-down) false)}]))))

(defn popup-window-container
  [_ owner opts]
  (reify
    om/IInitState
    (init-state [this]
      {:channel (chan)
       :side :bottom})
    om/IDidMount
    (did-mount [this]
       (let [node (om.core/get-node owner)
             parent (dommy/remove! node)
             target (if (:from opts)
                        (sel1 (:from opts))
                        parent)
             arrow (sel1 node ".arrow")]
       (dommy/append! (app-root) node)
       (om/update-state! owner (fn [st]
                                (merge st {:node node
                                           :parent parent})))
       (go-loop []
        (let [[message _] (alts! [(om/get-state owner :channel) (timeout 50)])]
          (when-not (= message :quit)
            (let [ofs-max (fn [o m]
                              (if (<  o 0)
                                (max o (- m))
                                (min o m)))
                  wz (window-size)
                  trect (dommy/bounding-client-rect target)
                  target-pos (merge trect
                                    {:top (+ (:top trect) (:scroll-y wz))
                                     :bottom (+ (:bottom trect) (:scroll-y wz))
                                     :left (+ (:left trect) (:scroll-x wz))
                                     :right (+ (:right trect) (:scroll-x wz))})
                  bounding-rect (dommy/bounding-client-rect node)

                  side (condp =  (om/get-state owner :prefered-side)
                        :top    (if (>= (- (:top target-pos) (+ (:height bounding-rect) 20)) (:scroll-y wz)) :top :bottom)
                        :bottom (if (<= (+ (:bottom target-pos) (:height bounding-rect)) (+ (:scroll-y wz) (- (:height wz) 20))) :bottom :top)
                        :right  (if (<= (+ (:right  target-pos) (:width  bounding-rect)) (+ (:scroll-x wz) (- (:width  wz) 20))) :right  :left)
                        :left   (if (>= (- (:left target-pos) (+ (:width bounding-rect) 20)) (:scroll-x wz)) :left :right))
                  y (condp = side
                           :top    (- (:top target-pos) (:height bounding-rect))
                           :bottom (:bottom target-pos)
                           :right  (- (+ (:top target-pos) (/ (:height target-pos) 2))
                                       (/ (:height bounding-rect) 2))
                           :left   (- (+ (:top target-pos) (/ (:height target-pos) 2))
                                       (/ (:height bounding-rect) 2)))

                  x (condp = side
                           :top    (- (+ (:left target-pos) (/ (:width target-pos) 2))
                                       (/ (:width bounding-rect) 2))
                           :bottom (- (+ (:left target-pos) (/ (:width target-pos) 2))
                                       (/ (:width bounding-rect) 2))
                           :right  (:right target-pos)
                           :left   (- (:left target-pos) (:width bounding-rect)))
                  offset-left (ofs-max (if (contains? #{:top :bottom} side)
                                        (cond (< x (:scroll-x wz)) (- (:scroll-x wz) x)
                                              (> (+ x (:width bounding-rect)) (+ (:width wz) (:scroll-x wz))) (-  (+ (:width wz) (:scroll-x wz)) (+ x (:width bounding-rect)))
                                              :else 0)
                                        0) (- (/ (:width bounding-rect) 2) 20))
                  arrow-left (* 100 (- 0.5 (/ offset-left (:width bounding-rect))))

                  offset-top (ofs-max (if (contains? #{:left :right} side)
                                        (cond (< y (:scroll-y wz)) (- (:scroll-y wz) y)
                                              (> (+ y (:height bounding-rect)) (+ (:height wz) (:scroll-y wz))) (-  (+ (:height wz) (:scroll-y wz)) (+ y (:height bounding-rect)))
                                              :else 0)
                                        0) (- (/ (:height bounding-rect) 2) 20))
                  arrow-top (* 100 (- 0.5 (/ offset-top (:height bounding-rect))))]
              (if-not (= (om/get-state owner :side) side)
                (om/set-state! owner :side side))
              (dommy/set-px! node :top (+ y offset-top) :left (+ x offset-left))
              (if (contains? #{:top :bottom} side)
                 (dommy/set-style! arrow :left (str arrow-left "%"))
                 (dommy/set-style! arrow :top (str arrow-top "%")))
              (recur)))))))
    om/IWillUnmount
    (will-unmount[this]
       (dommy/remove! (om/get-state owner :node))
       (dommy/append! (om/get-state owner :parent) (om/get-state owner :node))
       (put! (om/get-state owner :channel) :quit))
    om/IRenderState
    (render-state [this {:keys [label side content-fn] :as state}]
      (html
        [:div {:class (str "om-widgets-popup-window " (name side))}
          [:span {:class "arrow"}]
             (content-fn (:close-fn opts))]))))

(defn popup-window-component
  [_ owner opts]
  (reify
    om/IRenderState
    (render-state [this {:keys [visible-content-fn popup-content-fn visible prefered-side channel] :as state}]
      (dom/div nil
        (when visible
          (dom/div nil
            (om/build popup-window-overlay nil {:state {:mouse-down #(om/set-state! owner :visible false)}})
            (om/build popup-window-container nil {:state {:content-fn popup-content-fn
                                                          :prefered-side prefered-side}
                                                  :opts {:from (:from opts)
                                                         :close-fn #(om/set-state! owner :visible false)}})))

          (visible-content-fn (fn []
                              (om/update-state! owner (fn[st]
                                                    (merge st {:visible true})))))))))

(defn popup-window
  [visible-content-fn popup-content-fn & {:keys [prefered-side from] :or {prefered-side :bottom}}]
  (om/build popup-window-component nil {:state {:visible-content-fn visible-content-fn
                                                :popup-content-fn popup-content-fn
                                                :prefered-side prefered-side}
                                        :opts {:from from}}))
