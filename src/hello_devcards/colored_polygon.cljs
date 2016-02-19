(ns hello-devcards.colored-polygon
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

(defn app-state [resolution]
  (merge
   polygon/initial-app-state
   {:resolution resolution}))

(def initial-app-state
  (app-state 320))

(defn polygon [{:keys [class-name color points]}]
  (apply svg/polygon class-name color points))

(defn polygon-group [polygons]
  (apply svg/group-svg
         "polygons"
         (mapv polygon polygons)))

(def program-button-set-1
  [["Square"           (polygon/poly 4)]
   ["Triangle"         (polygon/poly 3)]
   ["Hexagon"          (polygon/poly 6)]
   ["Dodecagon"        (polygon/poly 12)]
   ["24 sided polygon" (polygon/poly 24)]])

(def command-button-set-1
  [["Forward"  (polygon/->Forward 1)]
   ["Backward" (polygon/->Forward -1)]
   ["Left15"     (polygon/->Turn 15)]
   ["Right15"    (polygon/->Turn -15)]
   ["Half"     (polygon/->Resize (/ 2))]
   ["Double"   (polygon/->Resize 2)]])

(defn svg-turtle [turtle]
  (svg/group-svg "turtle"
                 (svg/circle (:position turtle) 3)
                 (svg/line (:position turtle) (:tip turtle))))

(defn section-element
  [color points channel]
  [:polygon {:points (apply svg/points-str points)
             :fill color
             :stroke "grey"
             :on-click (u/send! channel color)}])

(defn section [{:keys [color points]} channel]
  (section-element color points channel))

(defn section-group [sections channel]
  (apply svg/group-svg
         "sections"
         (mapv #(section % channel) sections)))

(defn color-wheel-state [steps resolution]
  (let [state (reduce
               (fn [state command]
                 (polygon/process-command command state))
               (u/app-state resolution)
               (polygon/full-wheel steps))
        f (mappings/eigth resolution)
        t-state (polygon/transform-state f state)]
    t-state))

(def cw (color-wheel-state 12 320))

(defn color-wheel [state channel]
  (let [{:keys [resolution polygons]} state]
    [:svg {:width resolution :height resolution :class "board"}
     (section-group polygons channel)]))

(defn process-color-chan [channel state]
  (go (loop []
        (let [color (<! channel)]
          (println color)
          (swap! state
                 #(assoc-in % [:current-color] color))
          (recur)))))

(defn polygons
  [app-state]
  (let [app @app-state
        {:keys [resolution turtle]} app
        f (mappings/eigth resolution)
        ui-chan (chan)
        color-chan (chan)
        _ (u/process-channel ui-chan app-state)
        _ (process-color-chan color-chan app-state)
        a (polygon/transform-state f app)
        points (:points a)
        polygons (:polygons a)]
    [:div
     (u/command-buttons ui-chan command-button-set-1)
     (u/program-buttons ui-chan program-button-set-1)
     (color-wheel cw color-chan)
     [:svg {:width resolution :height resolution :class "board"}
      (when (not (empty? points))
        (apply svg/polyline "lines" points))
      (polygon-group polygons)
      (svg-turtle (:turtle a))]]))

(defcard-rg polygon-card
  "regular polygons with color"
  (fn [app _] [polygons app])
  (reagent/atom (u/app-state 320))
  {:inspect-data true :history true})

(comment
  (in-ns 'hello-devcards.colored-polygon)
  (color-wheel-state 12 320)
  )
