(ns hello-devcards.complex
  "a simple complex turtle"
  (:require [complex.number :as n]))

(def initial-turtle
  {:position n/zero
   :length 1
   :angle 0
   :orientation :counter-clockwise})

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

(defn in
  "decrease length by one as long as length is greater than one"
  [turtle]
  (let [length (:length turtle)]
    (cond
      (> length 1) (update-in turtle [:length] dec)
      (= length 1) (assoc-in turtle [:length] (/ 2))
      :else turtle)))

(defn out
  "increase length by one as long as length is greater than one"
  [turtle]
  (let [length (:length turtle)]
    (cond
      (< length 1) (assoc-in turtle [:length] 1)
      :else (update-in turtle [:length] inc))))

(defn toggle-orientation [orientation]
  (if (= orientation :counter-clockwise)
    :clockwise
    :counter-clockwise))

(defn reflect
  "reflect turtle in line along heading"
  [turtle]
  (update-in turtle [:orientation] toggle-orientation))

(defn length->lightness [length]
  (cond
    (< length 1) "80%"
    (= 1 length) "70%"
    (= 2 length) "50%"
    (= 3 length) "40%"
    (= 4 length) "30%"
    :else "20%"))

(defn color
  "return an hsl color string based on position of given turtle"
  [turtle]
  (let [{:keys [length angle]} turtle
        hue (mod angle 360)
        lightness (length->lightness length)]
    (str "hsl(" hue ", 100%, " lightness  ")")))

(comment
  (in-ns 'hello-devcards.complex)
  (-> initial-turtle (resize 2) (turn 90) (move 2))
  ;;=> {:position #complex.number.complex{:x 1, :y 2}, :length 2, :angle 90}

  (-> initial-turtle (resize 2) (turn 45) (move 2))
  (-> initial-turtle in)
  (-> initial-turtle out out out)

  (color initial-turtle)
  (color (turn initial-turtle -15))
  (color (-> initial-turtle out out))
  )
