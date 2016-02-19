(ns hello-devcards.utils
  "a temporary place to put reusable things"
  (:require
   [hello-devcards.polygon :as polygon]
   [cljs.core.async :as async :refer [>! <! put! chan alts! timeout]])
  (:require-macros
   [cljs.core.async.macros :refer [go]]))

(defn app-state [resolution]
  (merge
   polygon/initial-app-state
   {:resolution resolution}))

(def initial-app-state
  (app-state 640))

(defn process-channel
  [chan atom]
  (go (loop []
        (let [command (<! chan)]
          (swap! atom
                 #(polygon/process-command command %))
          (recur)))))

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
  "a single command button that outs a command on a channel"
  [ui-channel name command]
  [:button {:on-click (send! ui-channel command)
            :class "command"} name])

(defn command-buttons
  "gui for command buttons"
  [ui-channel button-set]
  (into [:div]
        (map #(apply command-button ui-channel %) button-set)))

(defn program-button
  "a single program button that puts a program on a channel"
  [ui-channel name program]
  [:button {:on-click #(run-program ui-channel program)
            :class "command"}
   name])

(defn program-buttons
  "program buttons"
  [ui-channel program-set]
  (into [:div] (map #(apply program-button ui-channel %) program-set)))
