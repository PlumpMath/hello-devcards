(ns hello-devcards.bezier
  (:require
   [devcards.core]
   [complex.number :as n :refer [zero one i negative-one negative-i infinity add sub mult div]]
   [reagent.core :as reagent]
   [sablono.core :as sab :include-macros true]
   [goog.events :as events])
  (:require-macros
   [reagent.ratom :as ratom :refer [reaction]]
   [devcards.core :as dc :refer [defcard deftest defcard-rg defcard-doc]])
  (:import [goog.events EventType]))

(enable-console-print!)

(comment
  (in-ns 'hello-devcards.bezier)
 )

(defn by-id [id]
  (.getElementById js/document id))

(defn bounding-client-rect [id]
  (let [el (by-id id)
        rect (.getBoundingClientRect el)]
    {:top (.-top rect)
     :left (.-left rect)}))

(defn handle-mouse-event [event point id]
  (let [bcr (bounding-client-rect id)
        e {:type (.-type event)
           :position [(- (.-clientX event) (:left bcr))
                      (- (.-clientY event) (:top bcr))]
           :point point
           :command (.-metaKey event)}]
    (println e)
    e))

(defcard quadratic-bezier
  "M 30 75 Q 240 30 300 120"
  (sab/html
   (let [id "quadratic-bezier"]
     [:div
      [:svg {:width 400 :height 400
             :id id}
       [:path {:d "M 30 75 Q 240 30 300 120"
               :stroke "red"
               :fill "none"}]
       [:circle {:cx 30 :cy 75 :r 5
                 :stroke "blue"
                 :fill "yellow"
                 :on-click #(handle-mouse-event % [30 75] id)}]
       [:circle {:cx 240 :cy 30 :r 5
                 :stroke "blue"
                 :fill "green"
                 :on-click #(handle-mouse-event % [240 30] id)}]
       [:circle {:cx 300 :cy 120 :r 5
                 :stroke "blue"
                 :fill "red"
                 :on-click #(handle-mouse-event % [300 120] id)}]]])))

(defcard quadratic-polybezier
  "M 30 100 Q 80 30 100 100 T 200 80"
  (sab/html
   [:div
    [:svg {:width 400 :height 400}
     [:path {:d "M 30 100 Q 80 30 100 100 T 200 80"
             :stroke "red"
             :fill "none"}]
     [:circle {:cx 30 :cy 100 :r 3 :stroke "green" :fill "none"}]
     [:circle {:cx 80 :cy 30 :r 3 :stroke "blue" :fill "none"}]
     [:circle {:cx 100 :cy 100 :r 3 :stroke "blue" :fill "none"}]
     [:circle {:cx 200 :cy 80 :r 3 :stroke "red" :fill "none"}]]]))

(defcard use-path
  (sab/html
   [:svg {:width 400 :height 400 :view-box "0 0 400 400"}
    [:defs
     [:g {:id "test"}
      [:line {:x1 0 :y1 0 :x2 50 :y2 0}]
      [:circle {:cx 50 :cy 0 :r 3 :stroke "green" :fill "none"}]]]
    [:use {:xlink-href "#test" :stroke "red"}]
    [:use {:xlink-href "#test" :transform "rotate(45)" :stroke "blue"}]
    [:use {:xlink-href "#test" :transform "translate(200, 200)" :stroke "green"}]
    [:use {:xlink-href "#test" :transform "translate(200, 200)" :stroke "green"}]
    [:use {:xlink-href "#test" :transform "translate(10,40) rotate(45) scale(4)" :stroke "purple"}]
    [:use {:xlink-href "#test" :transform "translate(200, 200) rotate(45)" :stroke "purple"}]
    [:use {:xlink-href "#test" :transform "rotate(10) translate(200, 200)" :stroke "purple"}]]))

(defcard arrow
  "a path defined in defs,reused with use, xlink-href and svg transforms"
  (sab/html
   [:div
    [:svg {:width 200 :height 200 :view-box "0 0 200 200"}
     [:defs
      [:g {:id "turtle-shell"}
       [:line {:x1 0 :y1 0 :x2 30 :y2 0}]
       [:circle {:cx 0 :cy 0 :r 3 :stroke "green" :fill "blue"}]
       [:path {:d "M 50 0 Q 40 0 30 10 Q 33 5 30 0"}]
       [:path {:d "M 50 0 Q 40 0 30 -10 Q 33 -5 30 0"}]]]
     [:use {:xlink-href "#turtle-shell"
            :transform "translate(20, 5) rotate(10) scale(1.5)"
            :stroke "purple"
            :fill "green"}]
     [:use {:xlink-href "#turtle-shell"
            :transform "translate(20, 20) rotate(30) scale(2)"
            :stroke "magenta"
            :fill "yellow"}]
     [:use {:xlink-href "#turtle-shell"
            :transform "translate(20, 70) rotate(60) scale(2.5)"
            :stroke "cyan"
            :fill "red"}]]]))

(defcard corner-bezier
  (sab/html
   [:div
    [:svg {:width 200 :height 200 :view-box "0 0 200 200"}
     [:defs
      [:g {:id "corner-bezier"}
       [:path {:d "M 0 0 Q 200 0 200 200 Q 0 200 0 0"}]]]
     [:use {:xlink-href "#corner-bezier"
            :stroke "purple"
            :fill "yellow"}]
     [:path {:d "M 0 0 L 200 0 200 200 0 200 0 0"
             :stroke "black"
             :fill "transparent"}]
     [:path {:d "M 0 0 L 200 200"
             :stroke "black"}]]]))

(def bezier-state
  {:start [30 75]
   :control [240 30]
   :end [300 120]})

(defn move-point-fn [id key atom]
  (fn [event]
    (let [bcr (bounding-client-rect id)
          new-point [(- (.-clientX event) (:left bcr))
                     (- (.-clientY event) (:top bcr))]]
      (swap! atom assoc key new-point))))

(defn drag-end-fn [drag-move drag-end-atom]
  (fn [evt]
    (events/unlisten js/window EventType.MOUSEMOVE drag-move)
    (events/unlisten js/window EventType.MOUSEUP @drag-end-atom)))

(defn dragging [on-drag]
  (let [drag-end-atom (atom nil)
        drag-end (drag-end-fn on-drag drag-end-atom)]
    (reset! drag-end-atom drag-end)
    (events/listen js/window EventType.MOUSEMOVE on-drag)
    (events/listen js/window EventType.MOUSEUP drag-end)))

(defn svg-point [point color id key app-state]
  (let [[cx cy] point]
    [:circle {:cx cx :cy cy :r 5 :stroke "blue" :fill color
              :on-mouse-down #(dragging (move-point-fn id key app-state))}]))

(defn svg-line [p1 p2 color]
  (let [[x1 y1] p1
        [x2 y2] p2]
    [:line {:x1 x1 :y1 y1 :x2 x2 :y2 y2
            :stroke color}]))

(defn svg-quadratic-bezier
  [{:keys [start control end]} color]
  (let [[sx sy] start
        [cx cy] control
        [ex ey] end
        path (str "M " sx " " sy " Q " cx " " cy " " ex " " ey)]
    [:path {:d path :stroke color :fill "none"}]))

(defn interactive-bezier [app-state]
  (let [app @app-state
        {:keys [start control end]} app
        id "bezier"]
    [:svg {:width 400 :height 400 :view-box "0 0 400 400"
           :id id}
     (svg-point control "green" id :control app-state)
     (svg-point start "yellow" id :start app-state)
     (svg-point end "red" id :end app-state)
     (svg-line start control "grey")
     (svg-line control end "grey")
     (svg-quadratic-bezier app "orange")]))

(defcard-rg interactive-bezier-card
  "## interactive quadratic bezier
### three draggable points:

* yellow is the start point,
* green is the control point and
* red is the end point

the quadratic bezier is orange"
  (fn [app _] [interactive-bezier app])
  (reagent/atom bezier-state)
  {:inspect-data true})
