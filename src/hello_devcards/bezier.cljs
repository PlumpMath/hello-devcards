(ns hello-devcards.bezier
  (:require
   [devcards.core]
   [complex.number :as n :refer [zero one i negative-one negative-i infinity add sub mult div]]
   [complex.vector :as v]
   [reagent.core :as reagent]
   [sablono.core :as sab :include-macros true]
   [thi.ng.geom.core :as g]
   [thi.ng.geom.svg.core :as svg]
   [thi.ng.color.core :as col])
  (:require-macros
   [reagent.ratom :as ratom :refer [reaction]]
   [devcards.core :as dc :refer [defcard deftest defcard-rg defcard-doc]]))

(comment
  (in-ns 'hello-devcards.bezier)
 )

(defcard quadratic-bezier
  "M 30 75 Q 240 30 300 120"
  (sab/html
   [:div
    [:svg {:width 400 :height 400}
     [:path {:d "M 30 75 Q 240 30 300 120"
             :stroke "red"
             :fill "none"}]
     [:circle {:cx 30 :cy 75 :r 3 :stroke "blue" :fill "none"}]
     [:circle {:cx 240 :cy 30 :r 3 :stroke "blue" :fill "none"}]
     [:circle {:cx 300 :cy 120 :r 3 :stroke "blue" :fill "none"}]]]))

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
            :transform "translate(50, 50) rotate(30) scale(2)"
            :stroke "purple"
            :fill "orange"}]]]))
