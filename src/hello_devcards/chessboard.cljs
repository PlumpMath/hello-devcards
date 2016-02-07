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

(def init-app-state
  {:position [20 40]
   :heading :east
   :scale 1})

(def app-state (reagent/atom init-app-state))

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
   [:button {:on-click (send! ui-channel (p/->Forward (* 32  1)))}   "Forward"]
   [:button {:on-click (send! ui-channel (p/->Forward (* 32 -1)))}   "Backward"]
   [:button {:on-click (send! ui-channel (p/->Left))}                "Left"]
   [:button {:on-click (send! ui-channel (p/->Right))}               "Right"]
   [:button {:on-click (send! ui-channel (p/->Resize (/ 2)))}        "Half"]
   [:button {:on-click (send! ui-channel (p/->Resize 2))}            "Double"]])

(defn process-channel [turtle-channel]
  (go (loop []
        (let [command (<! turtle-channel)]
          (swap! app-state #(p/process-command command %))
          (recur)))))

(defn transform-str [turtle]
  (let [{:keys [position heading scale]} turtle
        [x y] position
        angle (p/heading->angle heading)]
    (svg/transform-str position angle scale)))

(defn board
  "an svg chessboard using a pixie turtle"
  [app-atate]
  (let [turtle @app-state
        chan (chan)
        _ (process-channel chan)]
    [:div
     (command-buttons chan)
     [:svg {:width 200 :height 200}
      (svg/defs (svg/straight-arrow "arrow" 32))
      (svg/use-path "#arrow" (transform-str turtle) "turtle")]]))

(defcard-rg chessboard
  "a chessboard"
  [board app-state]
  app-state
  {:inspect-data true :history true})

(comment
  (in-ns 'hello-devcards.chessboard)
  (board)
  ;;=>

  )
