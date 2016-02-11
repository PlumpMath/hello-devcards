(ns hello-devcards.core
  (:require
   [devcards.core]
   [complex.number :as n :refer [zero one i negative-one negative-i infinity add sub mult div]]
   [hello-devcards.circle]
   [hello-devcards.bezier]
   [hello-devcards.pixie-turtle]
   [hello-devcards.chessboard]
   [reagent.core :as reagent]
   [timothypratley.reanimated.core :as anim]
   [sablono.core :as sab :include-macros true])
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

(def four-color [:lt-red :lt-blue :lt-purple :lt-green])

(defonce index (reagent/atom 0))

(defn l [index n]
  (mod (+ index n) 4))

(defn c [index n]
  (four-color (l index n)))

(comment
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
      [:button {:on-click #(swap! index inc)} "right"]]]))

(defn main []
  ;; conditionally start the app based on wether the #main-app-area
  ;; node is on the page
  (if-let [node (.getElementById js/document "main-app-area")]
    (js/React.render (sab/html [:div "This is working"]) node)))

(main)

(comment
  (in-ns 'hello-devcards.core)
  (reset! n "Walter")
  (println @hiccup-ratum)
  )
