(ns hello-devcards.eigth
  (:require
   [hello-devcards.mappings :as m]
   [hello-devcards.complex :as turtle]
   [complex.number :as n]))

;; geometric transformations as data
;; using complex numbers
(defrecord Reflection [])
(defrecord Scale [s])
(defrecord Rotation [a])
(defrecord Translation [v])
(defrecord General [a b conj])
(defrecord Composition [sequence])

(def identity-transform (->General n/one n/zero false))

(defrecord Round [])

(defn eigth
  "a mapping from the complex numbers into screen coordinates
  of the given resolution
  where:
  zero is mapped to the midpoint and
  four steps (or two doubles) gets a turtle to the edge and
  up is really up"
  [resolution]
  (let [s (/ resolution 8)
        m (/ resolution 2)
        screen-midpoint (n/c [m m])]
    (->Composition
     (list
      (->Reflection)
      (->Scale s)
      (->Translation screen-midpoint)))))

;; geometric objects
(defrecord Circle [center radius])
(defrecord Point [center])
(defrecord Turtle [position lentgh angle])

(def standard-turtle (->Turtle n/zero 1 0))

(defprotocol Transformable
  (transform [object transformation]))

(extend-protocol Transformable
  Turtle
  (transform [turtle transformation]
    )
  Circle
  (transform [circle transformation]
    )
  Point
  (transform [point transformation]
    ))


;; all transforms have inverse
(defprotocol Invertible
  (inverse [transformation]))

(extend-protocol Invertible
  Reflection
  (inverse [reflection] reflection)
  Scale
  (inverse [{s :s}] (->Scale (/ s)))
  Rotation
  (inverse [{a :a}] (->Rotation (- a)))
  Translation
  (inverse [{v :v}] (->Translation (n/minus v)))
  General
  (inverse [{a :a b :b conj :conj}]
    (let [c (n/recip a)
          d (n/times (n/minus b) (n/recip a))]
      (->General c d conj)))
  Composition
  (inverse [{sequence :sequence}]
    (->Composition (reverse (map inverse sequence)))))

(defprotocol Composible
  (compose [transform [a b conj]]))

(defn toggle [conj]
  (if (true? conj) false true))

(extend-protocol Composible
  Reflection
  (compose [_ [a b conj]]
    [(n/conjugate a) (n/conjugate b) (toggle conj)])
  Scale
  (compose [{s :s} [a b conj]]
    [(n/times a s) (n/times b s) conj])
  Rotation
  (compose [{angle :a} [a b conj]]
    (let [w (n/complex-polar angle)]
      [(n/times a w) (n/times b w) conj]))
  Translation
  (compose [{v :v} [a b conj]]
    [a (n/plus b v) conj])
  General
  (compose [{a1 :a b1 :b conj1 :conj} [a b conj]]
    (let [c (n/times a a1)
          d (n/plus (n/times b a1) b)]
      (if (false? conj1)
        [c d conj]
        [(n/conjugate c) (n/conjugate d) (toggle conj)])))
  Composition
  (compose [{s :sequence} [a b conj]]
    (reduce
     (fn [result transform]
       (compose transform result))
     [a b conj]
     s)))

(defn display-triple [[a b conj]]
  (let [f n/coords]
    [(f a) (f b) conj]))

(defn as-fn [transform]
  (let [i [n/one n/zero false]
        [a b conj] (compose transform i)]
    (fn [z]
      (if (false? conj)
        (n/plus (n/times a z) b)
        (n/plus (n/times a (n/conjugate z)) b)))))

(comment
  (require '[hello-devcards.eigth] :reload)
  (in-ns 'hello-devcards.eigth)
  (inverse (->Translation n/one))
  (inverse (->Rotation 30))
  (inverse (->Scale 2))
  (inverse (->Reflection))
  (inverse identity-transform)
  (let [a (n/complex-polar 2 30)
        b (n/c [1 2])
        f (->General a b false)]
    (inverse f))
  (transform standard-turtle (eigth 320))

  (let [i [n/one n/zero false]]
    (display-triple i))

  (let [i [n/one n/zero false]
        t (->Reflection)]
    (display-triple
     (compose t i)))

  (display-triple
   (reduce (fn [res trans]
             (compose trans res))
           [n/one n/zero false]
           (list
            (->Rotation 30)
            (->Scale 2)
            (->Translation n/i)
            (->Reflection))))

  (let [i [n/one n/zero false]
        t (eigth 320)]
    (display-triple
     (compose t i)))

  (let [f (as-fn (eigth 320))
        data [n/zero n/one n/i]]
    (mapv (comp n/coords f) data))
  ;;=> [[160 160] [200 160] [160 120]]
  )
