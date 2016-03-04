(ns hello-devcards.transforms
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
  {:t1 g/standard-turtle
   :turtle (-> g/standard-turtle
           (turtle/move (n/c [2 1]))
           (turtle/turn 15)
           (turtle/resize (/ 2)))
   :mapping (g/eigth 640)})

(def command-button-set-1
  [["Forward"  (polygon/->Forward 1)]
   ["Backward" (polygon/->Forward -1)]
   ["Left"     (polygon/->Turn 15)]
   ["Right"    (polygon/->Turn -15)]
   ["Half"     (polygon/->Resize (/ 2))]
   ["Double"   (polygon/->Resize 2)]])

(def command-button-set-2
  [["Reflect"    (g/->Reflection)]
   ["Rotate 90"  (g/->Rotation 90)]
   ["Rotate -90" (g/->Rotation -90)]
   ["Dilate 2"   (g/->Dilation 2)]
   ["Dilate 1/2" (g/->Dilation (/ 2))]])

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

(def lattice-points
  (:points (lattice/four-by-four-lattice lattice/initial-state)))

(defn lattice-point [p]
  (svg/circle p 3))

(defn render-lattice [lattice f]
  (let [g (comp lattice-point f)]
    (apply svg/group-svg "lattice" (map g lattice))))

(defn process-transform-chan [channel state]
  (go (loop []
        (let [transform (<! channel)]
          (println transform)
          (swap! state #(update-in % [:turtle] (fn [t] (g/transform t transform))))
          (recur)))))

(defn transforms
  [app-state]
  (let [app @app-state
        user->user (g/as-fn (:mapping app))
        resolution 640
        f (mappings/user->screen user->user)
        ui-chan (chan)
        transform-chan (chan)
        _ (u/process-channel ui-chan app-state)
        _ (process-transform-chan transform-chan app-state)]
    [:div
     (u/command-buttons ui-chan command-button-set-1)
     (u/command-buttons transform-chan command-button-set-2)
     [:svg {:width resolution :height resolution :class "board"}
      (svg-turtle (:t1 app) user->user "turtle")
      (svg-turtle (:turtle app) user->user "turtle2")
      (render-lattice lattice-points f)]]))

(defcard-rg transform-card
  "a transformation playground"
  (fn [app _] [transforms app])
  (reagent/atom initial-app-state))
