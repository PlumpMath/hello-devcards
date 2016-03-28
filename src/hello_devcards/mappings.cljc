(ns hello-devcards.mappings
  "mapping of complex numbers
  from complex numbers to complex numbers and
  from complex numbers to screen coordinates"
  (:require
   [complex.number :as n]
   [hello-devcards.geometry :as g]))

;; a maaping that maps unit circle
;; center to midpoint
;; radius to an eigth of screen spcace
;; where up is really up

(defn round
  "round given number"
  [n]
  (Math/round n))

(defn round-c
  "round a given complex number"
  [c]
  (mapv round c))

(def to-screen
  "maps a complex number into rounded coordinates"
  (comp round-c n/coords))

(defn user->screen
  "create a user->screen fn for given fn f
where f is a geometric transformation of complex numbers"
  [f]
  (comp to-screen f))

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

  (let [f (g/eigth 640)  ;; a transformation of user-space
        u->u (g/as-fn f) ;; transformation as a function
        u->s (user->screen u->u) ;; a user->screen function
        ]
    [(u->u n/zero)
     (u->s n/zero)])
  )
