(ns hello-devcards.projective-turtle
  (:require
   [devcards.core]
   [complex.number :as n :refer [zero one i negative-one negative-i infinity add sub mult div]]
   [complex.vector :as v]
   [hello-devcards.complex :as turtle]
   [hello-devcards.polygon :as polygon]
   [hello-devcards.lattice :as lattice]
   [hello-devcards.utils :as u]
   [hello-devcards.mappings :as mappings]
   [hello-devcards.svg :as svg]
   [hello-devcards.geometry :as g]
   [reagent.core :as reagent]
   [sablono.core :as sab :include-macros true]
   [goog.events :as events]
   [cljs.core.async :as async :refer [>! <! put! chan alts! timeout]])
  (:require-macros
   [devcards.core :as dc :refer [defcard deftest defcard-rg defcard-doc]]
   [cljs.core.async.macros :refer [go]])
  (:import [goog.events EventType]))

(defcard story
  "
# Projective Turtle

The turtle of this story is a plane turtle positioned at the origin
with a heading of unit length, which sends forth a single virtual turtle
by letting it move forward one step at a time.
Soon this turtle will move off of the screen. One option is to zoom out.
The other option, the one that is taken here, is to project the virtual turtle onto the unit circle that makes the shell of the original turtle.

For each step that the virtual turtle takes, a line is drawn from the virtual turtle's position,
back to the point i, which is the unit heading vector rotated 90 degrees counter-clockwise
 (to the left) of the perimeter ot the turtle's shell.

The point at which this line intersects the unit circle is the projection of the virtual turtle's position and is colored yellow, and the projecting line, orange.

The state consists of a sequence of positions
of the form [0 0] [1 0] [2 0] [3 0]

")

(defn initial-app-state [resolution fraction]
  {:standard-turtle g/standard-turtle
   :virtual-turtle g/standard-turtle
   :resolution resolution
   :mapping (g/fractional-mapping resolution fraction)
   :triple g/identity-triple})

(comment
  (let [f (g/fractional-mapping resolution fraction)]
    )
  )

(def medium-res-initial-state (initial-app-state 640 (/ 8)))

(defn svg-turtle [t user->user class-name]
  (let [f (mappings/user->screen user->user)
        p (:position t)
        t (turtle/tip t)
        pos (f p)
        tip (f t)
        l (n/length (n/sub (user->user p) (user->user t)))]
    (svg/group-svg class-name
                   (svg/circle pos l)
                   (svg/circle pos 3)
                   (svg/line pos tip))))

(defn projective-turtle
  [app-state]
  (let [app @app-state
        {:keys [resolution mapping triple standard-turtle]} app
        user->user (apply g/as-fn (g/compose mapping triple))]
    [:div
     [:svg {:width resolution :height resolution :class "turtle"}]
     (svg-turtle standard-turtle user->user "turtle")]))

(defcard-rg projective-card
  "A turtle projected"
  (fn [app _] [projective-turtle app])
  (reagent/atom medium-res-initial-state)
  {:inspect-data true :history true})
