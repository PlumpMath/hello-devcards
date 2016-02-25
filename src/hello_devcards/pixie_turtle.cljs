(ns hello-devcards.pixie-turtle
  (:require
   [devcards.core]
   [hello-devcards.pixie :as p]
   [hello-devcards.utils :as u]
   [reagent.core :as reagent]
   [cljs.core.async :as async :refer [>! <! put! chan alts! timeout]])
  (:require-macros
   [devcards.core :as dc :refer [defcard deftest defcard-rg defcard-doc]]
   [cljs.core.async.macros :refer [go]]))

(comment
  (in-ns 'hello-devcards.pixie-turtle)
  )

(enable-console-print!)

(def app-state (reagent/atom p/init-app-state))

(def command-button-set
  [["Forward"  (p/->Forward (* 40  1))]
   ["Backward" (p/->Forward (* 40 -1))]
   ["Left"     (p/->Left)]
   ["Right"    (p/->Right)]
   ["Half"     (p/->Resize (/ 2))]
   ["Double"   (p/->Resize 2)]])

(defn process-channel [turtle-channel]
  (go (loop []
        (let [command (<! turtle-channel)]
          (swap! app-state #(p/process-command command %))
          (recur)))))

(def pixie-path
  [:g {:id "pixie"}
   [:line {:x1 0 :y1 0 :x2 30 :y2 0}]
   [:circle {:cx 0 :cy 0 :r 3 :stroke "green" :fill "blue"}]
   [:path {:d "M 40 0 Q 30 0 30 10 Q 33 5 30 0"}]
   [:path {:d "M 40 0 Q 30 0 30 -10 Q 33 -5 30 0"}]])

(defn pixie-turtle [app-state]
  (let [{:keys [position heading scale resolution]} @app-state
        [x y] position
        ui-channel (chan)
        _ (process-channel ui-channel)]
    [:div
     (u/command-buttons ui-channel command-button-set)
     [:svg {:width resolution :height resolution :class "board"}
      [:defs
       pixie-path]
      [:use {:xlink-href "#pixie"
             :transform (str "translate(" x "," y ") "
                             "rotate(" (p/heading->angle heading) ") "
                             "scale(" scale ") ")
             :stroke "purple"
             :fill "orange"}]]]))

(defcard story
  "
in the beginning there was a turtle ...

a very simple turtle that responds to only six commands, for now

the turtles name is pixie

pixie can move forward and backward, turn left or right by 90 degrees, and double or half its size

the names of the six turtle commands are:

* forward
* backward
* left
* right
* half
* double

pixie lives in an svg element inside of a div element in an html page

you can inspect pixie in a browser window

clicking a button puts a turtle command onto a channel

the state of the turtle consists of

* a position in screen coordinates
* a heading - one of the four directions
* a scale

a processor listens on the channel for commands
when it receives a command it updates the state of the turtle

click on the command buttons and see the state change

what does each command do to the state?
")

(defcard-rg pixie
  "a pixel turtle in a finite discrete pixel space"
  [pixie-turtle app-state]
  app-state
  {:inspect-data true :history true})
