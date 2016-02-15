(ns hello-devcards.complex
  "a simple complex turtle"
  (:require [complex.number :as n]))

(def initial-turtle
  {:position n/zero
   :length 1
   :angle 0})

(defn heading [turtle]
  (let [{:keys [length angle]} turtle]
    (n/complex-polar length angle)))

(defn tip [turtle]
  (n/add (:position turtle) (heading turtle)))

(defn transform-turtle
  [f turtle]
  (let [p (f (:position turtle))
        t (f (tip turtle))]
    {:position p
     :tip t}))

(defn move
  "move given turtle by d units"
  [turtle d]
  (update-in turtle
             [:position]
             (fn [position]
               (n/add position
                      (n/times (heading turtle) d)))))

(defn turn
  "turn given turtle by a degrees"
  [turtle a]
  (update-in turtle
             [:angle]
             (fn [angle]
               (+ a angle))))

(defn resize
  "resize given turtle by factor s"
  [turtle s]
  (update-in turtle
             [:length]
             (fn [length]
               (* s length))))

(comment
  (in-ns 'hello-devcards.complex)
  (-> initial-turtle (resize 2) (turn 90) (move 2))
  ;;=> {:position #complex.number.complex{:x 1, :y 2}, :length 2, :angle 90}

  (-> initial-turtle (resize 2) (turn 45) (move 2))
  )
