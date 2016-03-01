 (ns hello-devcards.geometry
  "geometric objects and transforms and such"
  (:require
   [complex.number :as n]
   [hello-devcards.complex :as turtle]))

;; geometric transformations as data
;; using complex numbers
(defrecord Reflection [])
(defrecord Dilation [ratio])
(defrecord Rotation [angle])
(defrecord Translation [v])
(defrecord Affine [a b conj])
(defrecord Composition [sequence])

;; todo - for later add inversion in unit circle
(defrecord Mobius [a b c d conj])
(defrecord Inversion [])

(def identity-triple [n/one n/zero false])
(def identity-transform (apply ->Affine identity-triple))

;; geometric objects
(defrecord Circle [center radius])
(defrecord Point [center])
(defrecord Turtle [position length angle])

(def standard-turtle (map->Turtle turtle/initial-turtle))

;; all transforms have inverse
(defprotocol Invertible
  (inverse [transformation]))

(extend-protocol Invertible
  Reflection
  (inverse [reflection] reflection)
  Dilation
  (inverse [{r :ratio}] (->Dilation (/ r)))
  Rotation
  (inverse [{a :angle}] (->Rotation (- a)))
  Translation
  (inverse [{v :v}] (->Translation (n/minus v)))
  Affine
  (inverse [{a :a b :b conj :conj}]
    (let [c (n/recip a)
          d (n/times (n/minus b) (n/recip a))]
      (->Affine c d conj)))
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
  Dilation
  (compose [{r :ratio} [a b conj]]
    [(n/times a r) (n/times b r) conj])
  Rotation
  (compose [{angle :angle} [a b conj]]
    (let [w (n/complex-polar angle)]
      [(n/times a w) (n/times b w) conj]))
  Translation
  (compose [{v :v} [a b conj]]
    [a (n/plus b v) conj])
  Affine
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

(defn eigth
  "a mapping from the complex numbers into screen coordinates
  of the given resolution
  where:
  zero is mapped to the midpoint and
  four steps (or two doubles) gets a turtle to the edge and
  up is really up"
  [resolution]
  (let [r (/ resolution 8)
        m (/ resolution 2)
        screen-midpoint (n/c [m m])]
    (->Composition
     (list
      (->Reflection)
      (->Dilation r)
      (->Translation screen-midpoint)))))

(defprotocol Transformable
  (transform [object transformation]))

(extend-protocol Transformable
  Turtle
  (transform [turtle transformation]
    (let [f (as-fn transformation)]
      (cond
        (instance? Translation transformation)
        (update-in turtle [:position] f)

        (instance? Reflection transformation)
        (-> turtle
            (update-in [:position] f)
            (update-in [:angle] #(- %)))

        (instance? Rotation transformation)
        (let [angle (:angle transformation)]
          (-> turtle
              (update-in [:position] f)
              (update-in [:angle] #(+ % angle))))

        (instance? Dilation transformation)
        (-> turtle
            (update-in [:position] f)
            (update-in [:length] f))

        (instance? Affine transformation)
        (let [{a :a b :b conj :conj} transformation
              angle (n/rad->deg (n/arg a))
              ratio (n/length a)
              seq (if (false? conj)
                    (list (->Rotation angle)
                          (->Dilation ratio))
                    (list (->Rotation angle)
                          (->Dilation ratio)
                          (->Reflection)))]
          (transform turtle (->Composition seq)))

        (instance? Composition transformation)
        (reduce transform turtle (:sequence transformation)))))
  Circle
  (transform [circle transformation]
    (let [f (as-fn transformation)
          {:keys [center radius]} circle
          v (n/plus center (n/times n/one radius))]
      (-> circle
          (update-in [:center] f)
          (assoc-in [:radius] (n/length (n/sub (f center) (f v)))))))
  Point
  (transform [point transformation]
    (let [f (as-fn transformation)]
      (update-in point [:center] f))))

(comment
  (require '[hello-devcards.geometry] :reload)
  (in-ns 'hello-devcards.geometry)
  (inverse (->Translation n/one))
  (inverse (->Rotation 30))
  (inverse (->Dilation 2))
  (inverse (->Reflection))
  (inverse identity-transform)
  (let [a (n/complex-polar 2 30)
        b (n/c [1 2])
        f (->Affine a b false)]
    (inverse f))
  (transform standard-turtle (eigth 320))

  (display-triple identity-triple)

  (let [t (->Reflection)]
    (display-triple
     (compose t identity-triple)))

  (display-triple
   (reduce (fn [res trans]
             (compose trans res))
           identity-triple
           (list
            (->Rotation 30)
            (->Dilation 2)
            (->Translation n/i)
            (->Reflection))))

  (let [t (eigth 320)]
    (display-triple
     (compose t identity-triple)))

  (let [f (as-fn (eigth 320))
        data [n/zero n/one n/i]]
    (mapv (comp n/coords f) data))
  ;;=> [[160 160] [200 160] [160 120]]

  (let [f (as-fn (inverse (eigth 320)))
        data [[160 160] [200 160] [160 120]]]
    (mapv (comp n/coords f n/c) data))
  ;;=> [[0 0] [1N 0N] [0N 1N]]

  (turtle/move standard-turtle 1)
  (turtle/move standard-turtle (n/c [2 3]))
  (turtle/turn standard-turtle 30)

  (let [zero (->Point n/zero)
        t (->Translation n/one)]
    (transform zero t))

  (let [c (->Circle n/zero 1)
        t (->Translation n/one)]
    (transform c t))

  (let [c (->Circle n/zero 1)
        t (->Composition
           (list
            (->Dilation 2)
            (->Translation n/one)))]
    (transform c t))

  (transform standard-turtle (->Translation (n/c [2 3])))
  (transform standard-turtle (->Dilation 2))
  (transform standard-turtle
             (->Composition
              (list
               (->Rotation 30)
               (->Reflection))))
  (transform standard-turtle
             (->Composition
              (list
               (->Translation (n/c [2 3]))
               (->Reflection))))
  (transform standard-turtle (->Affine (n/c [2 3]) n/i false))
  )
