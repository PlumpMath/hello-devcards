(ns hello-devcards.color-wheel
  (:require
   [devcards.core]
   [complex.number :as n]
   [hello-devcards.complex :as turtle]
   [hello-devcards.polygon :as polygon]
   [hello-devcards.mappings :as mappings]
   [hello-devcards.svg :as svg]
   [reagent.core :as reagent]
   [cljs.core.async :as async :refer [>! <! put! chan alts! timeout]])
  (:require-macros
   [devcards.core :as dc :refer [defcard deftest defcard-rg defcard-doc]]
   [cljs.core.async.macros :refer [go]]))

(def config
  {:resolution 320})

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
      (println command)
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
  [ui-channel]
  (let [commands
        [["Forward"  (polygon/->Forward 1)]
         ["Backward" (polygon/->Forward -1)]
         ["Left"     (polygon/->Turn 15)]
         ["Right"    (polygon/->Turn -15)]
         ["Half"     (polygon/->Resize (/ 2))]
         ["Double"   (polygon/->Resize 2)]]]
    (into [:div]
          (map #(apply command-button ui-channel %) commands))))

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
   [:button {:on-click #(reset)
             :class "command"}
    "reset"]])

(defn user-turtle
  [app-state]
  (let [{:keys [turtle]} @app-state
        chan (chan)
        _ (process-channel chan app-state)]
    [:div
     (command-buttons chan)]))

(defn svg-turtle [turtle]
  (svg/group-svg "turtle"
                 (svg/circle (:position turtle) 3)
                 (svg/line (:position turtle) (:tip turtle))))

(defn polygon [{:keys [class-name points]}]
  (apply svg/polygon class-name points))

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
     (command-buttons chan)
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

(defcard-rg screen-turtle
  "turtle in screen coordinates"
  (fn [app _] [polygons app])
  app-state
  {:inspect-data true :history true})

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
  )
