(ns hello-devcards.chessboard
  (:require
   [devcards.core]
   [hello-devcards.pixie :as p]
   [hello-devcards.svg :as svg]
   [reagent.core :as reagent]
   [cljs.core.async :as async :refer [>! <! put! chan alts! timeout]])
  (:require-macros
   [devcards.core :as dc :refer [defcard deftest defcard-rg defcard-doc]]
   [cljs.core.async.macros :refer [go]]))

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
          (println command)
          (swap! app-state #(p/process-command command %))
          (recur)))))

(defn board
  "an svg chessboard using a pixie turtle"
  [app-atate]
  (let [turtle @app-state
        chan (chan)
        _ (process-channel chan)]
    [:div
     (command-buttons chan)
     [:svg {:width 800 :height 600}
      (svg/defs (svg/straight-arrow "arrow" 32))
      (svg/use-path "#arrow" (svg/transform-str [16 16] 0 2))]]))

(defcard-rg chessboard
  "a chessboard"
  [board app-state]
  app-state
  {:inspect-data true :history true})

(comment
  (board)
  ;;=>
  [:div
   [:svg {:width 800, :height 600}
    [:defs [:g {:id "arrow"}
            [:circle {:cx 0, :cy 0, :r 3}]
            [:path {:d "M 0 0 l 16 0 l 2 2 l 0 -4 l -2 2 "}]]]
    [:use {:xlink-href "#arrow",
           :transform "translate(16,16) rotate(0) scale(2) ",
           :stroke "black",
           :fill "transparent"}]]]
  )
