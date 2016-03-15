(ns hello-devcards.projective-turtle
  (:require
   [devcards.core]
   [complex.number :as n :refer [zero one i negative-one negative-i infinity add sub mult div]]
   [hello-devcards.complex :as turtle]
   [hello-devcards.mappings :as mappings]
   [hello-devcards.svg :as svg]
   [hello-devcards.geometry :as g]
   [reagent.core :as reagent]
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

The point at which this line intersects the unit circle is the projection of the virtual turtle's position and is colored green, and the projecting line, orange.

The state consists of a sequence of positions
of the form [0 0] [1 0] [2 0] [3 0]

")

;; initial state and state-transition functions
(defn initial-app-state [resolution fraction]
  {:standard-turtle g/standard-turtle
   :virtual-turtle g/standard-turtle
   :resolution resolution
   :mapping (g/fractional-mapping fraction resolution)
   :triple g/identity-triple
   :positions []})

(defn transition-state
  "a function of state that returns a transitioned state"
  [state]
  (let [new-turtle (turtle/move (:virtual-turtle state) 1)]
    (-> state
        (assoc-in [:virtual-turtle] new-turtle)
        (update-in [:positions] #(conj % (:position new-turtle))))))

(defn reset-state
  [state]
  (-> state
      (assoc-in [:virtual-turtle] g/standard-turtle)
      (assoc-in [:positions] [])))

(defn left-tip
  [turtle]
  (let [p (:position turtle)
        h (turtle/heading turtle)]
    (n/add p (n/times n/i h))))

(defn update-state-atom
  [state-atom]
  (swap! state-atom transition-state))

(defn reset-state-atom
  [state-atom]
  (swap! state-atom reset-state))

(defn function-button
  [name f]
  [:button {:on-click f :class "command"} name])

(defn function-buttons
  [app-state]
  [:div
   (function-button "Step"  (fn [_] (update-state-atom app-state)))
   (function-button "Reset" (fn [_] (reset-state-atom app-state)))])

(comment
  (in-ns 'hello-devcards.projective-turtle)

  (let [app-state (initial-app-state 640 (/ 8))
        {:keys [resolution mapping triple]} app-state
        user->user (apply g/as-fn (g/compose mapping triple))
        f (mappings/user->screen user->user)]
    (map f [n/zero n/one n/i]))
  ;;=> ([320 320] [400 320] [320 240])

  (let [app-state (atom (initial-app-state 640 (/ 8)))
        _ (println (keys @app-state))
        r (update-state-atom app-state)]
    (:positions @app-state))
  )

(def medium-res-initial-state         (initial-app-state 640 (/ 8)))
(def medium-res-initial-state-quarter (initial-app-state 640 (/ 4)))
(def medium-res-initial-state-half    (initial-app-state 640 (/ 2)))

(defn svg-turtle [t user->user class-name]
  (let [f (mappings/user->screen user->user)
        p (:position t)
        lt (left-tip t)
        t (turtle/tip t)
        pos (f p)
        tip (f t)
        l (n/length (n/sub (user->user p) (user->user t)))]
    (svg/group-svg class-name
                   (svg/circle pos l)
                   (svg/circle pos 3)
                   (svg/circle (f lt) 3)
                   (svg/line pos tip))))

(defn projective-line
  "draw line between from an to,
  point at to and point at intersection of line and unit-circle"
  [from to f]
  (let [n (:x to)
        d (+ (* n n) 1)
        intersection (n/c [(/ (* 2 n) d) (/ (- (* n n) 1) d)])]
    (svg/group-svg "projective-line"
                   (svg/line (f from) (f to))
                   (svg/circle (f intersection) 3)
                   (svg/circle (f to) 3))))

(defn projective-lines
  "draw line from left-tip of turtle to each position"
  [turtle positions user->user]
  (let [f (mappings/user->screen user->user)
        q (left-tip turtle)
        projective-line-fn (fn [position] (projective-line q position f))
        projective-lines (map projective-line-fn positions)]
    (apply svg/group-svg "projective-lines"
           projective-lines)))

(defn projective-turtle
  [app-state]
  (let [app @app-state
        {:keys [resolution mapping triple standard-turtle positions]} app
        user->user (apply g/as-fn (g/compose mapping triple))]
    [:div
     (function-buttons app-state)
     [:svg {:width resolution :height resolution :class "turtle"}
      (svg-turtle standard-turtle user->user "turtle")
      (projective-lines standard-turtle positions user->user)]]))

(defcard-rg projective-card
  "A turtle projected"
  (fn [app _] [projective-turtle app])
  (reagent/atom medium-res-initial-state-quarter)
  {:inspect-data true :history true})
