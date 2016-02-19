(ns hello-devcards.color-wheel
  (:require
   [devcards.core]
   [complex.number :as n]
   [hello-devcards.complex :as turtle]
   [hello-devcards.polygon :as polygon]
   [hello-devcards.utils :as u]
   [hello-devcards.mappings :as mappings]
   [hello-devcards.svg :as svg]
   [sablono.core :as sab :include-macros true]
   [reagent.core :as reagent]
   [cljs.core.async :as async :refer [>! <! put! chan alts! timeout]])
  (:require-macros
   [devcards.core :as dc :refer [defcard deftest defcard-rg defcard-doc]]
   [cljs.core.async.macros :refer [go]]))

(def initial-app-state
  (u/app-state 640))

(def command-button-set-1
  [["Forward"  (polygon/->Forward 1)]
   ["Backward" (polygon/->Forward -1)]
   ["Left15"     (polygon/->Turn 15)]
   ["Right15"    (polygon/->Turn -15)]
   ["Half"     (polygon/->Resize (/ 2))]
   ["Double"   (polygon/->Resize 2)]])

(defn user-turtle
  [app-state]
  (let [{:keys [turtle]} @app-state
        chan (chan)
        _ (u/process-channel chan app-state)]
    [:div
     (u/command-buttons chan command-button-set-1)]))

(defcard-rg app-state
  "Here, application state consists of a turtle,
vectors of points and polygons and a current color.
Command buttons change the turtle's state."
  (fn [app _] [user-turtle app])
  (reagent/atom polygon/initial-app-state)
  {:inspect-data true :history true})

(defn svg-turtle [turtle]
  (svg/group-svg "turtle"
                 (svg/circle (:position turtle) 3)
                 (svg/line (:position turtle) (:tip turtle))))

(defn section [{:keys [color points]}]
  (svg/section color points))

(defn section-group [sections]
  (apply svg/group-svg
         "sections"
         (mapv section sections)))

;; color wheel
(def color-wheel-button-set
  [["Left"     (polygon/->Turn 30)]
   ["Right"    (polygon/->Turn -30)]
   ["In"       (polygon/->In)]
   ["Out"      (polygon/->Out)]])

(def color-wheel-program-button-set
  [["Section" (polygon/section 12)]])

(defn color-wheel-one
  [app-state]
  (let [app @app-state
        {:keys [resolution turtle]} app
        chan (chan)
        _ (u/process-channel chan app-state)
        f (mappings/eigth resolution)
        a (polygon/transform-state f app)
        points (:points a)
        polygons (:polygons a)]
    [:div
     (u/command-buttons chan color-wheel-button-set)
     (u/program-buttons chan color-wheel-program-button-set)
     [:svg {:width resolution :height resolution :class "board"}
      (when (not (empty? points))
        (apply svg/polyline "lines" points))
      (section-group polygons)
      (svg-turtle (:turtle a))]]))

(defcard-rg twelve-step-color-section
  "12 step color wheel section with inspect-data and history"
  (fn [app _] [color-wheel-one app])
  (reagent/atom (u/app-state 320))
  {:inspect-data true :history true})

(defn color-wheel-two
  [app-state]
  (let [app @app-state
        {:keys [resolution turtle]} app
        chan (chan)
        _ (u/process-channel chan app-state)
        f (mappings/eigth resolution)
        a (polygon/transform-state f app)
        points (:points a)
        polygons (:polygons a)]
    [:div
     (u/program-buttons chan [["Make Wheel" (polygon/full-wheel 24)]])
     [:svg {:width resolution :height resolution :class "board"}
      (when (not (empty? points))
        (apply svg/polyline "lines" points))
      (section-group polygons)
      (svg-turtle (:turtle a))]]))

(defn color-wheel-three
  [app-state]
  (let [app @app-state
        {:keys [resolution turtle]} app
        chan (chan)
        _ (u/process-channel chan app-state)
        f (mappings/eigth resolution)
        a (polygon/transform-state f app)
        points (:points a)
        polygons (:polygons a)]
    [:div
     (u/program-buttons chan [["Make Wheel" (polygon/full-wheel 12)]])
     [:svg {:width resolution :height resolution :class "board"}
      (when (not (empty? points))
        (apply svg/polyline "lines" points))
      (section-group polygons)
      (svg-turtle (:turtle a))]]))

(defcard-rg twelve-step-color-wheel
  "small color wheel"
  (fn [app _] [color-wheel-three app])
  (reagent/atom (u/app-state 320)))

(defcard-rg twenty-four-step-color-wheel
  "large color wheel"
  (fn [app _] [color-wheel-two app])
  (reagent/atom initial-app-state))

(comment
  (in-ns 'hello-devcards.color-wheel)
  initial-app-state
  (let [f (mappings/eigth 320)]
    (turtle/transform-turtle f turtle/initial-turtle))

  (let [f (mappings/eigth 320)]
    (polygon/transform-state f initial-app-state))

  (let [f (mappings/eigth 320)
        state (reduce
               (fn [state command]
                 (polygon/process-command command state))
               initial-app-state
               (polygon/poly 4))]
    (polygon/transform-state f state))

  (let [f (mappings/eigth 320)
        state (reduce
               (fn [state command]
                 (polygon/process-command command state))
               initial-app-state
               (polygon/poly 4))
        t-state (polygon/transform-state f state)]
    (:polygons t-state))

  (let [f (mappings/eigth 320)
        state (reduce
               (fn [state command]
                 (polygon/process-command command state))
               initial-app-state
               (polygon/section 4))
        t-state (polygon/transform-state f state)]
    (:polygons t-state))

  (let [f (mappings/eigth 320)
        state (reduce
               (fn [state command]
                 (polygon/process-command command state))
               initial-app-state
               (polygon/full-wheel 12))
        t-state (polygon/transform-state f state)]
    (:polygons t-state))
  )
