(ns hello-devcards.pixie
  (:require [complex.vector :as v]
            #?(:clj
               [clojure.core.match :refer [match]]
               :cljs
               [cljs.core.match :refer-macros [match]])))

(def init-app-state
  {:position [160 160]
   :heading :east
   :scale 1
   :resolution 320})

(def heading->angle
  {:east 0
   :north -90
   :west 180
   :south 90})

(defrecord Forward [d])
(defrecord Left [])
(defrecord Right [])
(defrecord Resize [s])

(defprotocol Command
  (process-command [command app-state]))

(defn move [app d]
  (let [{:keys [position heading scale]} app]
    (match heading
           :east  (update-in app [:position] #(v/sum % [(* scale d) 0]))
           :west  (update-in app [:position] #(v/sum % [(* scale d -1) 0]))
           :north (update-in app [:position] #(v/sum % [0 (* scale d -1)]))
           :south (update-in app [:position] #(v/sum % [0 (* scale d)])))))

(defn dec-heading [heading]
  (match heading
         :east :north
         :north :west
         :west :south
         :south :east))

(defn inc-heading [heading]
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

(defn resize [app s]
  (update-in app [:scale] #(* % s)))

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
  Resize
  (process-command [{s :s} app]
    (resize app s)))

(comment
  (require '[hello-devcards.pixie] :reload)
  (in-ns 'hello-devcards.pixie)
  init-app-state
  (move init-app-state 1)
  (left init-app-state)
  )
