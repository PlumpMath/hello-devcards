(ns hello-devcards.circle
  (:require
   [complex.number :as n :refer [zero one i negative-one negative-i infinity add sub mult div]]
   [complex.vector :as v]
   [reagent.core :as reagent]
   [timothypratley.reanimated.core :as anim]
   [sablono.core :as sab :include-macros true]
   [thi.ng.geom.core :as g]
   [thi.ng.geom.svg.core :as svg]
   [thi.ng.color.core :as col])
  (:require-macros
   [reagent.ratom :as ratom :refer [reaction]]
   [devcards.core :as dc :refer [defcard deftest defcard-rg defcard-doc]]))

(def config
  {:domain [-2 2]
   :range [-2 2]
   :resolution [200 200]})

;; user space -> screen mapping stuff
(def round-pt (fn [p] (mapv Math.round p)))

(defn user->screen
  [config]
  (let [[xi xf] (:domain config)
        [yi yf] (:range config)
        [xres yres] (:resolution config)
        sx (/ xres (- xf xi))
        sy (/ yres (- yi yf))
        scale (v/scale sx sy)
        translate (v/translation [(- xi) (- yf)])]
    (fn [p]
      (if (number? p)
        (* sx p)
        ((comp round-pt scale translate) p)))))

(def t-fn (user->screen config))

(def g
  (comp t-fn n/coords))

(comment
  (g zero)
  ;;=> [100 100]
  (g one)
  ;;=> [150 100]
  (g i)
  ;;=> [100 50]
  )

(defn point [z]
  [:point (g z)])

(defn standard-circle [{:keys [center radius]}]
  [:circle {:center (g center) :radius (g radius)}])

(defn svg-comp [width height]
  [:svg {:width width :height height}])

(def c1 {:center zero :radius 1})
(def c2 {:center one :radius 1})

(defcard standard-circle
  "
## Standard Circle

### with style
 "
  [[:style {:stoke 'red :fill 'lt-red}]
   [:circle {:center 'zero :radius 1}]])

(def c3 [one i negative-i])
(def c4 [one negative-i negative-one])

(defcard three-point-circle
  "
## Three Point Circle

### specify a generalized circle by three points

say, for instance,

[one i negative-one]

or perhaps,

[one negative-i negative-one]")

(defcard parameterized-three-point
  "
## Parameterized Three Point Circle

### a parameterized circle passing through three given points

for parameter values of 0, 1 and infinity"
  )
