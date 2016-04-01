(ns hello-devcards.recursive-turtle
  (:refer-clojure :exclude [double])
  (:require
   [hello-devcards.polygon :as poly]))

(defn polygon
  "turtle program for drawing a regular polygon"
  [n]
  (let [a (/ 360 n)]
    (flatten
     (list
      (poly/->BeginPoly)
      (repeat n
              (list
               (poly/->Forward 1)
               (poly/->Turn a)
               (poly/->Point)
               (poly/->Pause 200)))
      (poly/->ClosePoly)))))

(defn sierp
  "produce one big long flat sequence of poly data"
  [n]
  (if (= n 0)
    (polygon 3)
    (flatten
     (repeat 3
             (list
              (poly/->Resize (/ 2))
              (sierp (dec n))
              (poly/->Resize 2)
              (poly/->Forward 1)
              (poly/->Turn 120))))))

(defn sierp-alt
  [n]
  (if (= n 0)
    [:poly 3]
    [:repeat 3
     [:resize (/ 2)]
     (sierp-alt (dec n))
     [:resize 2]
     [:move 1]
     [:turn 120]])
  )

(comment
  (require '[hello-devcards.recursive-turtle] :reload)
  (in-ns 'hello-devcards.recursive-turtle)
  (use 'clojure.repl)

  (sierp-alt 0)
  ;;=> [:poly 3]
  (sierp-alt 1)
  ;;=> [:repeat 3 [:resize 0.5] [:poly 3] [:resize 2] [:move 1] [:turn 120]]
  (sierp-alt 2)
  [:repeat 3
   [:resize 0.5]
   [:repeat 3
    [:resize 0.5]
    [:poly 3]
    [:resize 2]
    [:move 1]
    [:turn 120]]
   [:resize 2]
   [:move 1]
   [:turn 120]]
  )
