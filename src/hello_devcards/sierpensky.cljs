(ns hello-devcards.sierpensky
  (:require
   [devcards.core]
   [complex.number :as n]
   [hello-devcards.complex :as turtle]
   [hello-devcards.polygon :as polygon]
   [hello-devcards.utils :as u]
   [hello-devcards.mappings :as mappings]
   [hello-devcards.svg :as svg]
   [hello-devcards.recursive-turtle :as recursive]
   [reagent.core :as reagent]
   [cljs.core.async :as async :refer [>! <! put! chan alts! timeout]])
  (:require-macros
   [devcards.core :as dc :refer [defcard deftest defcard-rg defcard-doc]]
   [cljs.core.async.macros :refer [go]]))

(defcard story
  "
# Sierpensky Gasket")

(def program-button-set-1
  [["Triangle"     (recursive/polygon 3)]
   ["Sierpenski 0" (recursive/sierp4 0)]
   ["Sierpenski 1" (recursive/sierp4 1)]
   ["Sierpenski 2" (recursive/sierp4 2)]
   ["Sierpenski 3" (recursive/sierp4 3)]])

(def command-button-set-1
  [["Forward"  (polygon/->Forward 1)]
   ["Backward" (polygon/->Forward -1)]
   ["Left15"   (polygon/->Turn 15)]
   ["Right15"  (polygon/->Turn -15)]
   ["Half"     (polygon/->Resize (/ 2))]
   ["Double"   (polygon/->Resize 2)]])

(defn polygon [{:keys [class-name color points]}]
  (apply svg/polygon class-name color points))

(defn polygon-group [polygons]
  (apply svg/group-svg
         "polygons"
         (mapv polygon polygons)))

(defn svg-turtle [turtle]
  (svg/group-svg "turtle"
                 (svg/circle (:position turtle) 3)
                 (svg/line (:position turtle) (:tip turtle))))

(defn sierpensky
  [app-state]
  (let [app @app-state
        {:keys [resolution turtle current-color]} app
        f (mappings/eigth resolution)
        ui-chan (chan)
        color-chan (chan)
        _ (u/process-channel ui-chan app-state)
        a (polygon/transform-state f app)
        points (:points a)
        polygons (:polygons a)]
    [:div
     (u/command-buttons ui-chan command-button-set-1)
     (u/program-buttons ui-chan program-button-set-1)
     [:svg {:width resolution :height resolution :class "board"}
      (polygon-group polygons)
      (when (not (empty? points))
        (apply svg/polyline "lines" points))
      (svg-turtle (:turtle a))]]))

(defcard-rg sierpensky-card
  "sierpensky gasket"
  (fn [app _] [sierpensky app])
  (reagent/atom (u/app-state 320))
  {:history true})
