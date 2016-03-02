(ns hello-devcards.transforms
  (:require
   [devcards.core]
   [complex.number :as n :refer [zero one i negative-one negative-i infinity add sub mult div]]
   [complex.vector :as v]
   [reagent.core :as reagent]
   [sablono.core :as sab :include-macros true]
   [goog.events :as events])
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
