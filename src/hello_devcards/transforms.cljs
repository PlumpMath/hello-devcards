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
  "transformation playground

two turtles

a fixed yellow standard turtle and a movable orange turtle

start off with some simple transformations wrt the standard turtle

* reflection in x-axis
* rotation by 90 degrees
* dilation by ratio 2 or 1/2

that are applied to the orange turtle

next, we apply transformations to the perspective mapping

and finally, create a transform from the orange turtle to the yellow turtle and apply that to the perspective mapping
")

 (def initial-app-state
  {:st g/standard-turtle
   :turtle
   (-> g/standard-turtle
       (turtle/move (n/c [2 1]))
       (turtle/turn 15)
       (turtle/resize (/ 2)))
   :mapping (g/eigth 640)
   :triple g/identity-triple})

(def button-set-1
  [["Forward"  (polygon/->Forward 1)]
   ["Backward" (polygon/->Forward -1)]
   ["Left"     (polygon/->Turn 15)]
   ["Right"    (polygon/->Turn -15)]
   ["Half"     (polygon/->Resize (/ 2))]
   ["Double"   (polygon/->Resize 2)]])

(def button-set-2
  [["Reflect x-axis" (g/->Reflection)]
   ["Rotate 90"      (g/->Rotation 90)]
   ["Rotate -90"     (g/->Rotation -90)]
   ["Dilate 2"       (g/->Dilation 2)]
   ["Dilate 1/2"     (g/->Dilation (/ 2))]])

(def button-set-3
  [["Pan Left"   (g/->Translation n/one)]
   ["Pan Right"  (g/->Translation (n/minus n/one))]
   ["Pan Up"     (g/->Translation (n/minus n/i))]
   ["Pan Down"   (g/->Translation n/i)]
   ["Zoom in"    (g/->Dilation 2)]
   ["Zoom out"   (g/->Dilation (/ 2))]
   ["Rotate 15"  (g/->Rotation 15)]
   ["Rotate -15" (g/->Rotation -15)]])

(defn turtle-transform-fn
  "buttons to transforms perspective"
  [app-state channel]
  (fn [dom-event]
    (let [app @app-state
          {:keys [turtle]} app
          trans (g/turtle-transformation turtle)]
      (put! channel trans)
      (.stopPropagation dom-event))))

(defn reset-perspective-fn
  [app-state channel]
  (fn [dom-event]
    (let [app @app-state
          {:keys [triple]} app
          trans (apply g/->Affine triple)]
      (put! channel (g/inverse trans))
      (.stopPropagation dom-event))))

(defn fn-button [name f]
  [:button {:on-click f
            :class "command"} name])

(defn perspective-buttons
  [app-state channel]
  [:div
   (fn-button "t2->st" (turtle-transform-fn app-state channel))
   (fn-button "reset perspective" (reset-perspective-fn app-state channel))])

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
          (swap! state #(update-in % [:turtle] (fn [t] (g/transform t transform))))
          (recur)))))

(defn process-perspective-chan [channel state]
  (go (loop []
        (let [transform (<! channel)]
          (println transform)
          (swap! state #(update-in % [:triple] (fn [t] (g/compose transform t))))
          (recur)))))

(defn transforms
  [app-state]
  (let [app @app-state
        user->user (apply g/as-fn (g/compose (:mapping app) (:triple app)))
        resolution 640
        f (mappings/user->screen user->user)
        ui-chan (chan)
        transform-chan (chan)
        perspective-chan (chan)
        _ (u/process-channel ui-chan app-state)
        _ (process-transform-chan transform-chan app-state)
        _ (process-perspective-chan perspective-chan app-state)]
    [:div
     (u/command-buttons ui-chan button-set-1)
     (u/command-buttons transform-chan button-set-2)
     (u/command-buttons perspective-chan button-set-3)
     (perspective-buttons app-state perspective-chan)
     [:svg {:width resolution :height resolution :class "board"}
      (svg-turtle (:st app) user->user "turtle")
      (svg-turtle (:turtle app) user->user "turtle2")
      (render-lattice lattice-points f)]]))

(defcard-rg transform-card
  "a transformation playground"
  (fn [app _] [transforms app])
  (reagent/atom initial-app-state)
  {:history true})

(comment
  (in-ns 'hello-devcards.transforms)
  (:st initial-app-state)
  (:turtle initial-app-state)
  (g/turtle-transformation (:turtle initial-app-state))
  (let [{:keys [st turtle]} initial-app-state
        f (g/turtle-transformation turtle)]
    (= st (g/transform turtle f)))
  )
