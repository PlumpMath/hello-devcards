(ns hello-devcards.bezier
  (:require
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

(defcard hello-bezier
  "say hello to my new friend bezier quadratic"
  (sab/html
   [:div
    [:h1 "Hello Sablono"]
    [:svg {:width 400 :height 400}
     [:path {:d "M 30 75 Q 240 30 300 120"
             :stroke "red"
             :fill "none"}]
     [:circle {:cx 30 :cy 75 :r 3 :stroke "blue" :fill "none"}]
     [:circle {:cx 240 :cy 30 :r 3 :stroke "blue" :fill "none"}]
     [:circle {:cx 300 :cy 120 :r 3 :stroke "blue" :fill "none"}]]]))

(defcard quadratic-bezier
  "an svg bezier quadratic path d=M 30 100 Q 80 30 100 100 T 200 80"
  (sab/html
   [:div
    [:svg {:width 400 :height 400}
     [:path {:d "M 30 100 Q 80 30 100 100 T 200 80"
             :stroke "red"
             :fill "none"}]
     [:circle {:cx 30 :cy 100 :r 3 :stroke "blue" :fill "none"}]
     [:circle {:cx 80 :cy 30 :r 3 :stroke "blue" :fill "none"}]
     [:circle {:cx 100 :cy 100 :r 3 :stroke "blue" :fill "none"}]
     [:circle {:cx 200 :cy 80 :r 3 :stroke "blue" :fill "none"}]]]))
