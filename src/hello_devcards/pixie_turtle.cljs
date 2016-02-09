(ns hello-devcards.pixie-turtle
  (:require
   [devcards.core]
   [hello-devcards.pixie :as p]
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

(defn send!
  "Send information from the user to the message queue.
  The message must be a record which implements the Processor protocol."
  [channel message]
  (fn [dom-event]
    (put! channel message)
    (.stopPropagation dom-event)))

(defn command-buttons
  "gui with command buttons"
  [ui-channel]
  [:div
   [:button {:on-click (send! ui-channel (p/->Forward (* 40  1)))}   "Forward"]
   [:button {:on-click (send! ui-channel (p/->Forward (* 40 -1)))}   "Backward"]
   [:button {:on-click (send! ui-channel (p/->Left))}                "Left"]
   [:button {:on-click (send! ui-channel (p/->Right))}               "Right"]
   [:button {:on-click (send! ui-channel (p/->Resize (/ 2)))}        "Half"]
   [:button {:on-click (send! ui-channel (p/->Resize 2))}            "Double"]])

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
        chan (chan)
        _ (process-channel chan)]
    [:div
     (command-buttons chan)
     [:svg {:width resolution :height resolution :class "board"}
      [:defs
       pixie-path]
      [:use {:xlink-href "#pixie"
             :transform (str "translate(" x "," y ") "
                             "rotate(" (p/heading->angle heading) ") "
                             "scale(" scale ") ")
             :stroke "purple"
             :fill "orange"}]]]))

(defcard-rg pixie
  "a pixel turtle in a finite discrete pixel space"
  [pixie-turtle app-state]
  app-state
  {:inspect-data true :history true})
