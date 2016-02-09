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

(def init-turtle
  {:position [0 0]
   :heading :east
   :scale 1})

(def init-app-state
  {:turtle init-turtle
   :squares []})

(defn add-square [square app]
  (update-in app [:squares] #(conj % square)))

(def app-state (reagent/atom init-app-state))

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

(defn transform-str [turtle]
  (let [{:keys [position heading scale]} turtle
        [x y] position
        angle (p/heading->angle heading)]
    (svg/transform-str position angle scale)))

(defn square [ui-channel base class]
  (let [sq-prg (flatten (repeat 4 (list (p/->Forward base) (p/->Right))))]
    (go
      (doseq [command sq-prg]
        (<! (timeout 100))
        (>! ui-channel command))
      (<! (timeout 100))
      (swap! app-state
            (fn [state]
              (let [turtle (:turtle state)
                    {:keys [position scale]} turtle
                    sq [position (* scale base) class]]
                (add-square sq state)))))))

(defn command-buttons
  "gui with command buttons"
  [ui-channel base]
  (let [commands
        [["Forward"  (p/->Forward (* base  1))]
         ["Backward" (p/->Forward (* base -1))]
         ["Left"     (p/->Left)]
         ["Right"    (p/->Right)]
         ["Half"     (p/->Resize (/ 2))]
         ["Double"   (p/->Resize 2)]]]
    (into [:div]
          (map #(apply command-button ui-channel %) commands))))

(defn program-buttons
  "program buttons"
  [ui-channel base]
  [:div
   [:button {:on-click #(square ui-channel base "white")
             :class "command"}
    "White Square"]
   [:button {:on-click #(square ui-channel base "black")
             :class "command"}
    "Black Square"]])

(defn process-channel [turtle-channel]
  (go (loop []
        (let [command (<! turtle-channel)
              p-fn (fn [turtle] (p/process-command command turtle))]
          (swap! app-state
                 (fn [app]
                   (update-in app [:turtle] p-fn)))
          (recur)))))

(defn svg-square
  [[position scale class-name]]
  (svg/square class-name position scale))

(defn square-group
  "make some svg squares in a group"
  [squares]
  (let [svg-squares (mapv svg-square squares)]
    (into [:g {:class "squares"}]
          svg-squares)))

(defn board
  "an svg chessboard using a pixie turtle"
  [app-atate]
  (let [app @app-state
        turtle (:turtle app)
        squares (:squares app)
        chan (chan)
        _ (process-channel chan)
        unit-length 32]
    [:div
     (command-buttons chan unit-length)
     (program-buttons chan unit-length)
     [:svg {:width 200 :height 200 :class "board"}
      (svg/defs
        (svg/straight-arrow "arrow" unit-length))
      (square-group squares)
      (svg/use-path "#arrow" (transform-str turtle) "turtle")]]))

(defcard-rg chessboard
  "Lets build a chessboard, one square at a time, turtle style
&#9812;
"
  [board app-state]
  app-state
  {:inspect-data true :history true})

(comment
  (in-ns 'hello-devcards.chessboard)
  (board)
  ;;=>
  @app-state
  ;;=> {:turtle {:position [20 40], :heading :east, :scale 1}, :squares []}
  )
