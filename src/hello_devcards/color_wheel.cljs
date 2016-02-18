(ns hello-devcards.color-wheel
  (:require
   [devcards.core]
   [complex.number :as n]
   [hello-devcards.complex :as turtle]
   [hello-devcards.polygon :as polygon]
   [hello-devcards.mappings :as mappings]
   [hello-devcards.svg :as svg]
   [sablono.core :as sab :include-macros true]
   [reagent.core :as reagent]
   [cljs.core.async :as async :refer [>! <! put! chan alts! timeout]])
  (:require-macros
   [devcards.core :as dc :refer [defcard deftest defcard-rg defcard-doc]]
   [cljs.core.async.macros :refer [go]]))

(def config
  {:resolution 640})

(def initial-app-state
  (merge
   polygon/initial-app-state
   config))

(def app-state (reagent/atom initial-app-state))

(defn reset
  "reset app state to initial state"
  []
  (reset! app-state polygon/initial-app-state))

(defn process-channel
  ([chan]
   (go (loop []
         (let [command (<! chan)]
           (swap! app-state
                  #(polygon/process-command command %))
           (recur)))))
  ([chan atom]
   (go (loop []
         (let [command (<! chan)]
           (swap! atom
                  #(polygon/process-command command %))
           (recur))))))

(defn run-program [chan program]
  (go
    (doseq [command program]
      (cond
        (instance? polygon/Pause command)
        (<! (timeout (:delay command)))
        :else (>! chan command)))))

(defn send!
  "Send information from the user to the message queue.
  The message must be a record which implements the Processor protocol."
  [channel message]
  (fn [dom-event]
    (put! channel message)
    (.stopPropagation dom-event)))

(defn command-button
  "a single command button"
  [ui-channel name command]
  [:button {:on-click (send! ui-channel command)
            :class "command"} name])

(defn command-buttons
  "gui for command buttons"
  [ui-channel commands]
  (into [:div]
        (map #(apply command-button ui-channel %) commands)))

(def button-set-1
  [["Forward"  (polygon/->Forward 1)]
   ["Backward" (polygon/->Forward -1)]
   ["Left"     (polygon/->Turn 15)]
   ["Right"    (polygon/->Turn -15)]
   ["Half"     (polygon/->Resize (/ 2))]
   ["Double"   (polygon/->Resize 2)]])

(def button-set-2
  [["Forward"  (polygon/->Forward 1)]
   ["Backward" (polygon/->Forward -1)]
   ["Left"     (polygon/->Turn 15)]
   ["Right"    (polygon/->Turn -15)]
   ["In"       (polygon/->In)]
   ["Out"      (polygon/->Out)]])

(defn program-buttons
  "program buttons"
  [chan]
  [:div
   [:button {:on-click #(run-program chan (polygon/poly 4))
             :class "command"}
    "Square"]
   [:button {:on-click #(run-program chan (polygon/poly 3))
             :class "command"}
    "Triangle"]
   [:button {:on-click #(run-program chan (polygon/poly 6))
             :class "command"}
    "Hexagon"]
   [:button {:on-click #(run-program chan (polygon/poly 12))
             :class "command"}
    "Dodecagon"]
   [:button {:on-click #(run-program chan (polygon/poly 24))
             :class "command"}
    "24 sided polygon"]
   [:button {:on-click #(reset)
             :class "command"}
    "reset"]])

(defn user-turtle
  [app-state]
  (let [{:keys [turtle]} @app-state
        chan (chan)
        _ (process-channel chan app-state)]
    [:div
     (command-buttons chan button-set-1)]))

(defn svg-turtle [turtle]
  (svg/group-svg "turtle"
                 (svg/circle (:position turtle) 3)
                 (svg/line (:position turtle) (:tip turtle))))

(defn polygon [{:keys [class-name color points]}]
  (apply svg/polygon class-name color points))

(defn section [{:keys [color points]}]
  (svg/section color points))

(defn section-group [sections]
  (apply svg/group-svg
         "sections"
         (mapv section sections)))

(defn polygon-group [polygons]
  (apply svg/group-svg
   "polygons"
   (mapv polygon polygons)))

(defn polygons
  [app-state]
  (let [app @app-state
        {:keys [resolution turtle]} app
        f (mappings/eigth resolution)
        chan (chan)
        _ (process-channel chan app-state)
        a (polygon/transform-state f app)
        points (:points a)
        polygons (:polygons a)]
    [:div
     (command-buttons chan button-set-1)
     (program-buttons chan)
     [:svg {:width resolution :height resolution :class "board"}
      (when (not (empty? points))
        (apply svg/polyline "lines" points))
      (polygon-group polygons)
      (svg-turtle (:turtle a))]]))

(defcard-rg user-turtle
  "turtle in user coordinates"
  (fn [app _] [user-turtle app])
  (reagent/atom polygon/initial-app-state)
  {:inspect-data true :history true})

(defcard-rg polygon-card
  "regular polygons"
  (fn [app _] [polygons app])
  app-state
  {:history true})

;; color wheel
(def color-wheel-button-set
  [["Left"     (polygon/->Turn 30)]
   ["Right"    (polygon/->Turn -30)]
   ["In"       (polygon/->In)]
   ["Out"      (polygon/->Out)]])

(defn color-wheel-program-buttons
  "program buttons"
  [chan]
  [:div
   [:button {:on-click #(run-program chan (polygon/section 12))
             :class "command"}
    "Section"]])

(defn color-wheel-one
  [app-state]
  (let [app @app-state
        {:keys [resolution turtle]} app
        chan (chan)
        _ (process-channel chan app-state)
        f (mappings/eigth resolution)
        a (polygon/transform-state f app)
        points (:points a)
        polygons (:polygons a)]
    [:div
     (command-buttons chan color-wheel-button-set)
     (color-wheel-program-buttons chan)
     [:svg {:width resolution :height resolution :class "board"}
      (when (not (empty? points))
        (apply svg/polyline "lines" points))
      (section-group polygons)
      (svg-turtle (:turtle a))]]))

(defcard-rg twelve-step-color-wheel
  "12 step color wheel "
  (fn [app _] [color-wheel-one app])
  (reagent/atom initial-app-state)
  {:inspect-data true :history true})

(def color-wheel-button-set-two
  [["Left15"     (polygon/->Turn 15)]
   ["Right15"    (polygon/->Turn -15)]
   ["In"       (polygon/->In)]
   ["Out"      (polygon/->Out)]])

(defn color-wheel-program-buttons-two
  "program buttons"
  [chan]
  [:div
   [:button {:on-click #(run-program chan (polygon/section 24))
             :class "command"}
    "Section"]
   [:button {:on-click #(run-program chan (polygon/wheel 24))
             :class "command"}
    "Wheel"]])

(defn color-wheel-two
  [app-state]
  (let [app @app-state
        {:keys [resolution turtle]} app
        chan (chan)
        _ (process-channel chan app-state)
        f (mappings/eigth resolution)
        a (polygon/transform-state f app)
        points (:points a)
        polygons (:polygons a)]
    [:div
     (command-buttons chan color-wheel-button-set-two)
     (color-wheel-program-buttons-two chan)
     [:svg {:width resolution :height resolution :class "board"}
      (when (not (empty? points))
        (apply svg/polyline "lines" points))
      (section-group polygons)
      (svg-turtle (:turtle a))]]))

(defcard-rg twenty-four-step-color-wheel
  "24 step color wheel"
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
  )
