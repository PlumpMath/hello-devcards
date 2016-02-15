(ns hello-devcards.mappings
  "mapping from complex numbers in user space to screen space
  and back again"
  (:require
   [complex.number :as n]))

;; a maaping that maps unit circle
;; center to midpoint
;; radius to an eighth of screen spcace
;; where up is really up

(defn round
  "round given number"
  [n]
  (Math/round n))

(defn round-c
  "round a given complex number"
  [c]
  (mapv round c))

(defn mapping [resolution fraction]
  (let [k (/ resolution fraction)
        m (/ resolution 2)
        c (n/c [m m])]
    (fn [complex-number]
      (if (number? complex-number)
        (* k complex-number)
        (-> complex-number
            ;; flip scale translate round
            n/conjugate
            (n/times k)
            (n/plus c)
            (n/coords)
            round-c)))))

(defn eigth [resolution]
  (mapping resolution 8))

(comment
  (require '[hello-devcards.mappings] :reload)
  (in-ns 'hello-devcards.mappings)
  (let [f (eigth 320)]
    (f n/zero))
  (let [f (eigth 320)
        data [n/zero n/one n/i]]
    (mapv f data))
  )
