(ns hello-devcards.pixie-turtle
  (:require
   [devcards.core]
   [complex.number :as n :refer [zero one i negative-one negative-i infinity add sub mult div]]
   [complex.vector :as v]
   [reagent.core :as reagent]
   [sablono.core :as sab :include-macros true]
   [cljs.core.match :refer-macros [match]]
   [thi.ng.geom.core :as g]
   [thi.ng.geom.svg.core :as svg]
   [thi.ng.color.core :as col]
   [cljs.core.async :as async :refer [>! <! put! chan alts! timeout]])
  (:require-macros
   [reagent.ratom :as ratom :refer [reaction]]
   [devcards.core :as dc :refer [defcard deftest defcard-rg defcard-doc]]
   [cljs.core.async.macros :refer [go]]))

(comment
  (in-ns 'hello-devcards.pixie-turtle)
  )

(enable-console-print!)

(def init-app-state
  {:position [160 160]
   :heading :north
   :scale 1
   :resolution 320})

(def app-state (reagent/atom init-app-state))

(def heading->angle
  {:east 0
   :north 90
   :west 180
   :south -90})

(defn send!
  "Send information from the user to the message queue.
  The message must be a record which implements the Processor protocol."
  [channel message]
  (fn [dom-event]
    (put! channel message)
    (.stopPropagation dom-event)))

(defrecord Forward [d])
(defrecord Left [])
(defrecord Right [])
(defrecord Half [])
(defrecord Double [])

(defprotocol Command
  (process-command [command app-state]))

(defn move [app d]
  (let [{:keys [position heading scale]} app]
    (match heading
           :east (update-in app [:position] #(v/sum % [(* scale d 40) 0]))
           :west (update-in app [:position] #(v/sum % [(* scale d -40) 0]))
           :north (update-in app [:position] #(v/sum % [0 (* scale d 40)]))
           :south (update-in app [:position] #(v/sum % [0 (* scale d -40)])))))

(defn inc-heading [heading]
  (match heading
         :east :north
         :north :west
         :west :south
         :south :east))

(defn dec-heading [heading]
  (match heading
         :east :south
         :south :west
         :west :north
         :north :east))

(defn left [app]
  (let [{:keys [position heading scale]} app]
    (update-in app [:heading] dec-heading)))

(defn right [app]
  (update-in app [:heading] inc-heading))

(defn half [app]
  (update-in app [:scale] #(* % (/ 2))))

(defn double* [app]
  (update-in app [:scale] #(* % 2)))

(extend-protocol Command
  Forward
  (process-command [{d :d} app]
    (move app d))
  Left
  (process-command [_ app]
    (left app))
  Right
  (process-command [_ app]
    (right app))
  Half
  (process-command [{s :s} app]
    (half app))
  Double
  (process-command [{s :s} app]
    (double* app)))

(defn command-buttons
  "gui with command buttons"
  [ui-channel]
  [:div
   [:button {:on-click (send! ui-channel (->Forward 1))}  "Forward"]
   [:button {:on-click (send! ui-channel (->Forward -1))} "Backward"]
   [:button {:on-click (send! ui-channel (->Left))}       "Left"]
   [:button {:on-click (send! ui-channel (->Right))}      "Right"]
   [:button {:on-click (send! ui-channel (->Half))}       "Half"]
   [:button {:on-click (send! ui-channel (->Double))}     "Double"]])

(defn process-channel [turtle-channel]
  (go (loop []
        (let [command (<! turtle-channel)]
          (swap! app-state #(process-command command %))
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
        chan (chan)
        _ (process-channel chan)]
    [:div
     (command-buttons chan)
     [:svg {:width resolution :height resolution}
      [:defs
       pixie-path]
      [:use {:xlink-href "#pixie"
             :transform (str "translate(" x "," y ") "
                             "rotate(" (heading->angle heading) ") "
                             "scale(" scale ") ")
             :stroke "purple"
             :fill "orange"}]]]))

(defcard-rg pixie
  "a pixel turtle in a finite discrete pixel space"
  [pixie-turtle app-state]
  app-state
  {:inspect-data true :history true})
