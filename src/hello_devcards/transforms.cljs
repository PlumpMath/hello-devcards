(ns hello-devcards.transforms
  (:require
   [devcards.core]
   [complex.number :as n :refer [zero one i negative-one negative-i infinity add sub mult div]]
   [complex.vector :as v]
   [hello-devcards.complex :as turtle]
   [hello-devcards.polygon :as polygon]
   [hello-devcards.utils :as u]
   [hello-devcards.mappings :as mappings]
   [hello-devcards.svg :as svg]
   [reagent.core :as reagent]
   [sablono.core :as sab :include-macros true]
   [goog.events :as events]
   [cljs.core.async :as async :refer [>! <! put! chan alts! timeout]])
  (:require-macros
   [reagent.ratom :as ratom :refer [reaction]]
   [devcards.core :as dc :refer [defcard deftest defcard-rg defcard-doc]])
  (:import [goog.events EventType]))

(defcard story
  "transformation playgroud

two turtles

a standard turtle and a transformed tutle (transformed by sending it turtle commands)

start off with some simple transformations wrt the standard turtle

* translation by 1 or i
* reflection in x-axis
* rotation by 90 degrees
* dilation by ratio 2 or 1/2

these are applied to the transformed turtle
")

(def initial-app-state
  {:t1 turtle/initial-turtle
   :t2 (-> turtle/initial-turtle
           (turtle/move (n/c [2 3]))
           (turtle/turn 15)
           (turtle/resize (/ 2)))})

(def command-button-set-1
  [["Forward"  (polygon/->Forward 1)]
   ["Backward" (polygon/->Forward -1)]
   ["Left15"     (polygon/->Turn 15)]
   ["Right15"    (polygon/->Turn -15)]
   ["Half"     (polygon/->Resize (/ 2))]
   ["Double"   (polygon/->Resize 2)]])

(def program-button-set-1
  [["Triangle"         (polygon/poly 3)]
   ["Square"           (polygon/poly 4)]
   ["Hexagon"          (polygon/poly 6)]
   ["Dodecagon"        (polygon/poly 12)]
   ["24 sided polygon" (polygon/poly 24)]])

(defn svg-turtle [t f class-name]
  (let [turtle (turtle/transform-turtle f t)]
    (svg/group-svg class-name
                   (svg/circle (:position turtle) 3)
                   (svg/line (:position turtle) (:tip turtle)))))

(defn transforms
  [app-state]
  (let [app @app-state
        resolution 640
        f (mappings/eigth resolution)
        ui-chan (chan)]
    [:div
     (u/command-buttons ui-chan command-button-set-1)
     (u/program-buttons ui-chan program-button-set-1)
     [:svg {:width resolution :height resolution :class "board"}
      (svg-turtle (:t1 app) f "turtle")
      (svg-turtle (:t2 app) f "turtle2")]]))

(defcard-rg transform-card
  "a transformation playground"
  (fn [app _] [transforms app])
  (reagent/atom initial-app-state))
