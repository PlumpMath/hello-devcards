(ns hello-devcards.core
  (:require
   [devcards.core]
   [complex.number :as n :refer [zero one i negative-one negative-i infinity add sub mult div]]
   [hello-devcards.circle]
   [hello-devcards.bezier]
   [hello-devcards.pixie-turtle]
   [reagent.core :as reagent]
   [timothypratley.reanimated.core :as anim]
   [sablono.core :as sab :include-macros true]
   [thi.ng.geom.core :as g]
   [thi.ng.geom.svg.core :as svg]
   [thi.ng.color.core :as col])
  (:require-macros
   [reagent.ratom :as ratom :refer [reaction]]
   [devcards.core :as dc :refer [defcard deftest defcard-rg defcard-doc]]))

(enable-console-print!)

(defcard complex-turtle
  "
turtle defines a coordinate system
using only a few simple powers:

* turtle can move forward and turtle can move backward,
* turtle can spin left and spin right, by 90 degrees
* turtle can draw while he moves

"
  )

(defcard circles
  "
turtle likes circles

a circle has a center and a radius

turtle can draw a circle at any point,
of unit radius (for now)

turtle can also fill the inside of a circle
and the outside
")

(defn main []
  ;; conditionally start the app based on wether the #main-app-area
  ;; node is on the page
  (if-let [node (.getElementById js/document "main-app-area")]
    (js/React.render (sab/html [:div "This is working"]) node)))

(main)

;; remember to run lein figwheel and then browse to
;; http://localhost:3449/cards.html

;; color values
(def red       (col/rgba 1 0 0 1.0))
(def lt-red    (col/rgba 1 0 0 0.25))
(def green     (col/rgba 0 1 0 1.0))
(def lt-green  (col/rgba 0 1 0 0.25))
(def blue      (col/rgba 0 0 1 1.0))
(def lt-blue   (col/rgba 0 0 1 0.25))
(def lt-grey   (col/rgba 0.1 0.1 0.1 0.25))
(def lt-purple (col/rgba .8 0 0.8 0.25))
(def clear     (col/rgba 0 0 0 0.0))

(defn two-color-circle-style
  [edge inside]
  {:edge edge
   :inside inside})

(defn circle-style->svg
  [{:keys [edge inside]}]
  {:stroke edge :fill inside})

(def style-1
  (two-color-circle-style red lt-red))

(defn circle [center radius edge inside]
  {:center center
   :radius radius
   :style {:inside lt-red
           :edge red}})

(def origin [100 100])

(def circle1
  {:center [100 100]
   :radius 50
   :style (two-color-circle-style red lt-red)})

(def circle2
  {:center [150 100]
   :radius 50
   :style (two-color-circle-style green lt-green)})

(def circle3
  {:center [125 (- 100 (* 50 (Math/sqrt 3) (/ 2)))]
   :radius 50
   :style (two-color-circle-style blue lt-blue)})

(def three-circle-point-set
  (let [v (- 100 (* 50 (Math/sqrt 3) (/ 2)))
        w (+ 100 (* 50 (Math/sqrt 3) (/ 2)))]
    [[75 v] [175 v] [125 w]]))

(defn as-svg [circle]
  (svg/circle (:center circle)
              (:radius circle)
              (circle-style->svg (:style circle))))

(defn c->css [color]
  @(col/as-css color))

(defn circle-svg-component1
  []
  [:div [:svg {:width 300 :height 300}
         (as-svg circle1)]])

(defcard-rg one
  "one red circle

a center at zero and a radius of 1"
  [circle-svg-component1])

(defn circle-svg-component2
  []
  [:div [:svg {:width 300 :height 300}
         (as-svg circle1)
         (as-svg circle2)]])

(defcard-rg two
  "
trasnform first circle: translate by 1 step forward, change color to green

two circles:
one red,
two green

two points of intersection: omega and omega-bar"
  [circle-svg-component2])

(defn circle-svg-component3
  []
  [:div [:svg {:width 300 :height 300}
         (as-svg circle1)
         (as-svg circle2)
         (as-svg circle3)]])

(defcard-rg three
  "transform first circle: translate to omega, change color to blue

  three circles:
  one red,
  two green,
  three blue

  three centers:
  zero one omega

  three points of intersection:
  one + omega,
  omega - one,
  one - omega"
  [circle-svg-component3])

;; four circle system

(def c0
  {:center [150 150]
   :radius 50
   :style (two-color-circle-style red lt-red)})
(def c02
  {:center [150 150]
   :radius 100
   :style (two-color-circle-style red lt-grey)})
(def c1
  {:center [200 150]
   :radius 50
   :style (two-color-circle-style green lt-green)})
(def c2
  {:center [100 150]
   :radius 50
   :style (two-color-circle-style green lt-green)})
(def c3
  {:center [150 200]
   :radius 50
   :style (two-color-circle-style blue lt-blue)})
(def c4
  {:center [150 100]
   :radius 50
   :style (two-color-circle-style blue lt-blue)})

(def square-set
  (let [circles [c0 c1 c2 c3 c4 c02]]
    (map as-svg circles)))

(defn circle-svg-component4
  []
  [:div
   (into [:svg {:width 300 :height 300}]
         square-set)])

(defcard-rg four
  "starting with a red circle

  color green, translate by one, negative-one,
  color blue, tranlsate by i, negative-i

  highlight centers and intersections
  by drawing points"
  [circle-svg-component4])

(defcard-doc
  "## bottum up code"

  "start with data: circle geometry and style as data"
  (dc/mkdn-pprint-source circle1)

  (dc/mkdn-pprint-source two-color-circle-style)

  "circle component"
  (dc/mkdn-pprint-source circle-svg-component1)

  "making use of thi-ng's svg,
   in namespace [thi.ng.geom.svg.core :as svg]"
  (dc/mkdn-pprint-source as-svg)
  "
  the result of running:
```clojure
(as-svg circle1)
```
  is just data, in a form used by hiccup and soblano:"
  (as-svg circle1)

  "the devcards reagent macro defcard-rg
  renders the component circle-svg-component1 to html:
```clojure
(defcard-rg one
  [circle-svg-component1])
```"

  "circle-svg-component4"
  (dc/mkdn-pprint-source circle-svg-component4)
  (dc/mkdn-pprint-source square-set)
  (dc/mkdn-pprint-source c0)
  "..."
  )

(defn clipping-comp []
  [:div [:svg {:width 300 :height 300}
         [:defs
          [:clipPath {:id "clip"}
           [:path
            {:d
             "
M 150 50 L 250 150 L 150 250 L 50 150 L 150 50
M 150 100 L 100 150 L 150 200 L 200 150 L 150 100
"
             :stroke "green" :fill "none"}]]]

         [:rect {:x 0 :y 0
                 :width "100%" :height "100%"
                 :fill "rgba(10,10,10,0.25)"}]
         (into [:g {:clip-path "url(#clip)"}] square-set)]])

(defn clipping-comp2 []
  [:div [:svg {:width 300 :height 300}
         [:defs
          [:clipPath {:id "clip"}
           [:path
            {:d
             "M 150 100 L 100 150 L 150 200 L 200 150 L 150 100"
             :stroke "green" :fill "none"}]]]

         [:rect {:x 0 :y 0
                 :width "100%" :height "100%"
                 :fill "rgba(10,10,10,0.25)"}]
         (into [:g {:clip-path "url(#clip)"}] square-set)]])

(defn clipping-comp3 []
  (let [[[p1x p1y] [p2x p2y] [p3x p3y]] three-circle-point-set
        path (str "M " p1x " " p1y " L " p2x " " p2y " L " p3x " " p3y " L " p1x " " p1y)]
    [:div [:svg {:width 300 :height 300}
           [:defs
            [:clipPath {:id "clip"}
             [:path
              {:d
               path
               :stroke "green" :fill "none"}]]]
           [:g {:clip-path "url(#clip)"}
            (as-svg circle1)
            (as-svg circle2)
            (as-svg circle3)]]]))

(defcard-rg clipping-masking
  "## Clipping and Masking

  goal is to fill the outside of a circle

with out drawing anything in the middle"
  [clipping-comp])

(defcard-rg clipping-masking2
  "## Clipping and Masking 2"
  [clipping-comp2])

(defcard-rg clipping-masking3
  "## Clipping and Masking 3"
  [clipping-comp3])

(def four-color (mapv c->css [lt-red lt-blue lt-purple lt-green]))

(defonce index (reagent/atom 0))

(defn l [index n]
  (mod (+ index n) 4))

(defn c [index n]
  (four-color (l index n)))

(defn arc-comp []
  (let [arc "A 50 50 0 0 0 100 50"
        arc2 "A 50 50 0 0 0 50 100"
        arc3 "A 50 50 0 0 0 100 150"
        arc4 "A 50 50 0 0 0 150 100"
        i @index]
    [:svg {:width 200 :height 299}
     [:path {:d (str "M 100 100 L 150 100" arc "Z")
             :fill (c @index 0) :stroke "white" :stroke-width 2}]
     [:path {:d (str "M 100 100 L 100 50" arc2 "Z")
             :fill (c @index 1) :stroke "white"}]
     [:path {:d (str "M 100 100 L 50 100" arc3 "Z")
             :fill (c @index 2) :stroke "white" :stroke-width 2}]
     [:path {:d (str "M 100 100 L 100 150" arc4 "Z")
             :fill (c @index 3) :stroke "white" :stroke-width 1}]
     [:circle {:cx 100 :cy 100 :r 4 :fill "white"}]
     [:circle {:cx 150 :cy 100 :r 5 :fill "white"}]
     [:circle {:cx 100 :cy 50 :r 5 :fill "white"}]
     [:circle {:cx 50 :cy 100 :r 5 :fill "white"}]
     [:circle {:cx 100 :cy 150 :r 5 :fill "white"}]]))

(defn arc-comp-recip []
  (let [arc "A 50 50 0 0 0 100 50"
        arc2 "A 50 50 0 0 0 50 100"
        arc3 "A 50 50 0 0 0 100 150"
        arc4 "A 50 50 0 0 0 150 100"]
    [:svg {:width 200 :height 299}
     [:path {:d (str "M 200 100 L 150 100" arc "L 100 0 200 0 Z")
             :fill (c @index 3) :stroke "white" :stroke-width 2}]
     [:path {:d (str "M 100 0 L 100 50" arc2 "L 0 100 0 0 Z")
             :fill (c @index 2) :stroke "white"}]
     [:path {:d (str "M 0 100 L 50 100" arc3 "L 100 200 0 200 Z")
             :fill (c @index 1) :stroke "white" :stroke-width 2}]
     [:path {:d (str "M 100 200 L 100 150" arc4 "L 200 100 200 200 Z")
             :fill (c @index 0) :stroke "white" :stroke-width 1}]
     [:circle {:cx 100 :cy 100 :r 4 :fill "white"}]
     [:circle {:cx 150 :cy 100 :r 5 :fill "white"}]
     [:circle {:cx 100 :cy 50 :r 5 :fill "white"}]
     [:circle {:cx 50 :cy 100 :r 5 :fill "white"}]
     [:circle {:cx 100 :cy 150 :r 5 :fill "white"}]]))

(defcard-rg arc
  "a quater arc painted turtle and its reciprocal"
  [:div
   [arc-comp]
   [arc-comp-recip]
   [:div
    [:button {:on-click #(swap! index dec)} "left"]
    [:button {:on-click #(swap! index inc)} "right"]]])

(comment
  (in-ns 'hello-devcards.core)
  (reset! n "Walter")
  (println @hiccup-ratum)
  )
