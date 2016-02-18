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

(defn initial-app-state [base resolution]
  {:turtle init-turtle
   :polyline []
   :squares []
   :pen :up
   :base base
   :resolution resolution})

(def init-app-state
  (initial-app-state 32 (* 4 32)))

(def board-app-state
  (initial-app-state 32 (* 8 32)))

(def big-board-app-state
  (initial-app-state 64 (* 8 64)))

(defn add-point [app point]
  (update-in app [:polyline] #(conj % point)))

(defn add-square [app square]
  (update-in app [:squares] #(conj % square)))

(defn send!
  "Send information from the user to the message queue.
  The message must be a record which implements the Processor protocol."
  [channel message]
  (fn [dom-event]
    (put! channel message)
    (.stopPropagation dom-event)))

(defn transform-str [turtle]
  (let [{:keys [position heading scale]} turtle
        [x y] position
        angle (p/heading->angle heading)]
    (svg/transform-str position angle scale)))

(defrecord Penup [])
(defrecord Pendown [])
(defrecord ClosePoly [c])

(defrecord Pause [delay])

(defprotocol ChessCommand
  (process-command [command app-state]))

(extend-protocol ChessCommand
  Penup
  (process-command [_ app]
    (let [pen (:pen app)]
      (if (= pen :down)
        (-> app
            (assoc-in [:pen] :up)
            (assoc-in [:polyline] []))
        app)))
  Pendown
  (process-command [_ app]
    (let [{:keys [turtle pen polyline]} app]
      ;; do nothing if pen is already down
      (if (= pen :up)
        (-> app
            (assoc-in [:pen] :down)
            (assoc-in [:polyline] [(:position turtle)]))
        app)))
  ClosePoly
  (process-command [{c :c} app]
    (-> app
        (add-square {:class-name c :points (drop-last (:polyline app))})
        (assoc-in [:polyline] [])
        (assoc-in [:pen] :up))))

(defn square-program [base color]
  (vec (flatten
           (list
            (->Pendown)
            (flatten
             (repeat 4
                     [(p/->Forward base)
                      (p/->Right)
                      (->Pause 30)]))
            (->ClosePoly color)))))

(defn four-square [base]
  (flatten
   (repeat 2
           (concat
            (conj (square-program base "white")
                  (p/->Right))
            (conj (square-program base "black")
                  (p/->Right))))))

(defn row [base n]
  (flatten
   (repeat n
           (concat
            (conj (square-program base "white")
                  (p/->Forward base))
            (conj (square-program base "black")
                  (p/->Forward base))))))

(defn two-row [base n]
  (concat
   (row base n)
   (list (p/->Right) (p/->Forward (* 2 base)) (p/->Right))
   (row base n)
   (list (p/->Right)(p/->Right))))

(defn four-row [base row-length]
  (flatten (repeat 2 (two-row base row-length))))

(defn board-program [base]
  (flatten (repeat 4 (two-row base 4))))

(defn run-program [chan program]
  (go
    (doseq [command program]
      (cond
        (instance? Pause command) (<! (timeout (:delay command)))
        :else (>! chan command)))))

(defn command-button
  "a single command button"
  [ui-channel name command]
  [:button {:on-click (send! ui-channel command)
            :class "command"} name])

(defn command-buttons
  "gui for command buttons"
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

(defn square-buttons
  "program buttons"
  [ui-channel base]
  [:div
   [:button {:on-click #(run-program ui-channel (square-program base "white"))
             :class "command"}
    "White Square"]
   [:button {:on-click #(run-program ui-channel (square-program base "black"))
             :class "command"}
    "Black Square"]])

(defn board-buttons
  "program buttons"
  [ui-channel base]
  [:div
   [:button {:on-click #(run-program ui-channel (board-program base))
             :class "command"}
    "Draw Board"]])

(defn pixie? [command]
  (satisfies? p/Command command))

(defn update-state
  "return new state for given command"
  [state command]
  (let [{:keys [turtle polyline squares pen]} state]
    (if (pixie? command)
      (let [new-turtle (p/process-command command turtle)]
        (if (and (= :down pen) (instance? p/Forward command))
          (-> state
              (assoc-in [:turtle] new-turtle)
              (add-point (:position new-turtle)))
          (assoc-in state [:turtle] new-turtle)))
      (process-command command state))))

(defn process-channel [turtle-channel app-state]
  (go (loop []
        (let [command (<! turtle-channel)]
          (swap! app-state #(update-state % command))
          (recur)))))

(defn svg-square
  [{:keys [class-name points]}]
  (apply svg/polygon class-name nil points))

(defn square-group
  "make some svg squares in a group"
  [squares]
  (let [svg-squares (mapv svg-square squares)]
    (into [:g {:class "squares"}]
          svg-squares)))

(defn square
  "an svg chessboard using a pixie turtle"
  [app-state]
  (let [{:keys [turtle squares polyline base resolution]} @app-state
        chan (chan)
        _ (process-channel chan app-state)]
    [:div
     (command-buttons chan base)
     (square-buttons chan base)
     [:svg {:width resolution :height resolution :class "board"}
      (svg/defs
        (svg/straight-arrow "arrow" base))
      (when (not (empty? polyline))
        (apply svg/polyline "lines" polyline))
      (square-group squares)
      (svg/use-path "#arrow" (transform-str turtle) "turtle")]]))

(defn board
  "an svg chessboard using a pixie turtle"
  [app-state]
  (let [{:keys [turtle squares polyline base resolution]} @app-state
        chan (chan)
        _ (process-channel chan app-state)]
    [:div
     (board-buttons chan base)
     [:svg {:width resolution :height resolution :class "board"}
      (when (not (empty? polyline))
        (apply svg/polyline "lines" polyline))
      (square-group squares)]]))

(defcard-rg chessboard-squares
  "Lets build a chessboard, one square at a time, turtle style
&#9812;
"
  (fn [app _] [square app])
  (reagent/atom init-app-state)
  {:inspect-data true :history true})

(defcard-rg chessboard-little
  "Draw a little board
&#9812;
"
  (fn [app _]
    [board app])
  (reagent/atom board-app-state))

(defcard-rg chessboard-big
  "Draw a big board
&#9812;
"
  (fn [app _]
    [board app])
  (reagent/atom big-board-app-state))

(comment
  (in-ns 'hello-devcards.chessboard)

  (square-program 1 "white")

  (update-state init-app-state (p/->Forward 1))
  (update-state init-app-state (->Pendown))
  (reduce update-state init-app-state
          [(->Pendown)
           (p/->Forward 1)])
  (reduce update-state init-app-state
          [(->Pendown)
           (p/->Forward 1)
           (->Penup)])
  (reduce update-state init-app-state
          [(->Pendown)
           (p/->Forward 1)
           (p/->Right)
           (p/->Forward 1)
           (p/->Right)
           (p/->Forward 1)
           (p/->Right)
           (p/->Forward 1)
           (p/->Right)
           (->ClosePoly :white)])

  (four-square 1))
