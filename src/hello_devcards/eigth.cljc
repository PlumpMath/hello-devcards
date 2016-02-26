(ns hello-devcards.eigth
  (:require
   [hello-devcards.geometry :as g]
   [complex.number :as n]))

(defn eigth-transform [resolution]
  (let [s (/ resolution 8)
        m (/ resolution 2)
        screen-midpoint (n/c [m m])]
    (g/->Composition
     (list
      (g/->Reflection)
      (g/->Scale s)
      (g/->Translation screen-midpoint)))))

(defn eigth
  "a mapping from the complex numbers into screen coordinates
  of the given resolution
  where:
  zero is mapped to the midpoint and
  four steps (or two doubles) gets a turtle to the edge and
  up is really up"
  [resolution]
  (g/as-fn (eigth-transform resolution)))

(defn inverse-eigth [resolution]
  (g/as-fn (g/inverse (eigth-transform resolution))))

(comment
  (require '[hello-devcards.eigth] :reload)
  (in-ns 'hello-devcards.eigth)

  (let [f (eigth 320)
        data [n/zero n/one n/i]]
    (mapv (comp n/coords f) data))
  ;;=> [[160 160] [200 160] [160 120]]

  (let [f (inverse-eigth 320)
        data [[160 160] [200 160] [160 120]]]
    (mapv (comp n/coords f n/c) data))
  ;;=> [[0 0] [1N 0N] [0N 1N]]

  )
