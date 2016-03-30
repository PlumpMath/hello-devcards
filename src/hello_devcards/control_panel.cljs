(ns hello-devcards.control-panel
  "a turtle control panel component
  that puts data commands on a channel"
  (:require
   [devcards.core]
   [reagent.core :as reagent]
   [complex.number :as n :refer [zero one i length sub]]
   [hello-devcards.polygon :as polygon]
   [hello-devcards.geometry :as g]
   [hello-devcards.svg :as svg]
   [hello-devcards.utils :as u]
   [hello-devcards.mappings :as mappings]
   [cljs.core.async :as async :refer [>! <! put! chan alts! timeout]])
  (:require-macros
   [devcards.core :as dc :refer [defcard deftest defcard-rg defcard-doc]]
   [cljs.core.async.macros :refer [go]]))

(defcard story
  "An svg control panel for a turtle that puts data on a channel
when svg buttons are clicked.

A control-panel function takes a channel
and returns a div containg clickable svg components,
that send data to the channel.")

(defn polygon
  [class-name points data channel]
  [:polygon {:points (apply svg/points-str points)
             :class class-name
             :on-click (u/send! channel data)}])

(defn straight-arrow
  "map straight arrow by given function f"
  [f class-name data channel]
  (let [l1 (n/times one (/ 5 8))
        h1 (n/times i (/ 8))
        h2 (n/times i (/ 3 8))
        p1 h1
        p2 (n/plus l1 h1)
        p3 (n/plus l1 h2)
        [p5 p6 p7] (mapv n/conjugate [p3 p2 p1])]
    (polygon class-name
             (map f [zero p1 p2 p3 one
                     p5 p6 p7 zero])
             data
             channel)))

(defn turn-arrow
  [f class-name data channel]
  (let [p1 (n/c [(/ 3 8) (/ 4)])
        p2 (n/c [0 (/ 5 8)])
        p3 (n/c [(/ 3 8) 1])]
    (polygon class-name
             (map f [p1 p2 p3])
             data
             channel)))

(defn control-panel [resolution channel]
  (let [user->user (g/as-fn (g/half resolution))
        user->user-2 (g/as-fn (g/->Composition (list (g/->Rotation 180) (g/half resolution))))
        user->user-3 (g/as-fn (g/->Composition (list (g/->Reflection) (g/half resolution))))
        f (mappings/user->screen user->user)
        f-2 (mappings/user->screen user->user-2)
        f-3 (mappings/user->screen user->user-3)
        l (length (sub (user->user one) (user->user zero)))]
    [:div {:class "control-panel"}
     [:svg {:width resolution :height resolution}
      (svg/circle (f zero) (* l (/ 5 8)))
      (svg/circle (f zero) (* l (/ 7 8)))
      (turn-arrow f "left-arrow" (polygon/->Turn 15) channel)
      (turn-arrow f-3 "right-arrow" (polygon/->Turn -15) channel)
      (straight-arrow f "forward-arrow" (polygon/->Forward 1) channel)
      (straight-arrow f-2 "backward-arrow" (polygon/->Forward -1) channel)]]))

(defn process-channel-debug
  "prints out all events from channel to console"
  [channel]
  (go (loop []
        (when-let [data (<! channel)]
          (println data)
          (recur)))))

(defn control-panel-div
  [app-state]
  (let [channel (chan)
        _ (process-channel-debug channel)
        resolution 200]
    (control-panel resolution channel)))

(defcard-rg control-pannel-card
  "devcard for developing control-panel component"
  (fn [app _] [control-panel-div app])
  (reagent/atom {}))
