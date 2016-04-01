(ns hello-devcards.protocols
  (:require
   [complex.number :as n]))

(defrecord Point [z])
(defrecord Vector [z])

(defn complex-vector
  "a vector has length and  angle
  by a complex number"
  ([length angle] (->Vector (n/complex-polar length angle)))
  ([z] (->Vector z)))

(defn point [z] (->Point z))

(defprotocol Turtle
  (move [turtle d])
  (turn [turtle a])
  (resize [turtle r])
  (reflect [turtle]))

(defrecord Complex-Turtle [position heading orientation])

(defn turtle
  "turtle constructor function taking either
  a complex number and length and angle or
  a point and a vector"
  ([position length angle]
   (turtle (point position) (complex-vector length angle)))
  ([position heading]
   (->Complex-Turtle position heading :counter-clockwise)))

(def initial-turtle (turtle n/zero 1 0))

;; Transforms
(defprotocol Transform
  (inverse [transformation])
  (transform-fn [transform]))

;; geometric transformations as data
;; using complex numbers
(defrecord Reflection []
  Transform
  (inverse [reflection] reflection)
  (transform-fn [reflection] (fn [z] (n/conjugate z))))

(defrecord Dilation [ratio]
  Transform
  (inverse [{:keys [ratio]}] (->Dilation (/ ratio)))
  (transform-fn [{:keys [ratio]}] (fn [z] (n/times z ratio))))

(defrecord Rotation [angle]
  Transform
  (inverse [rotation] (->Rotation (- (:angle rotation))))
  (transform-fn [rotation] (fn [z] (n/times (n/complex-polar (:angle rotation)) z))))

(defrecord Translation [v]
  Transform
  (inverse [translation] (->Translation (n/minus v)))
  (transform-fn [translation] (fn [z] (n/add z (:v translation)))))

(defrecord Affine [a b]
  Transform
  (inverse [{:keys [a b]}]
    (let [c (n/recip a)
          d (n/times (n/minus b) (n/recip a))]
      (->Affine c d)))
  (transform-fn [{:keys [a b]}]
    (fn [z] (n/plus (n/times a z) b))))

(defrecord Composition [sequence]
  Transform
  (inverse [{sequence :sequence}]
    (->Composition (reverse (map inverse sequence))))
  (transform-fn [{sequence :sequence}]
    (apply comp sequence)))

(defrecord Mobius [a b c d])
(defrecord Inversion [])
(defrecord Reciprocal [])

(defn mobius
  [a b c d] (->Mobius a b c d))

(defprotocol Transformable
  (transform [object transformation]))

(defn toggle-orientation [orientation]
  (if (= orientation :counter-clockwise)
    :clockwise
    :counter-clockwise))

(extend-protocol Transformable
  Vector
  (transform [vector transformation]
    (let [f (transform-fn transformation)]
      (cond
        (instance? Translation transformation)
        ;; vectors don't translate
        vector

        (instance? Reflection transformation)
        (update-in vector [:z] n/conjugate)

        (instance? Rotation transformation)
        (let [{:keys [angle]} transformation]
          (update-in vector [:z] #(n/times (n/complex-polar angle) %)))

        (instance? Dilation transformation)
        (let [{:keys [ratio]} transformation]
          (update-in vector [:z] #(n/times % ratio)))

        (instance? Affine transformation)
        (let [{:keys [a b]} transformation]
          (update-in vector [:z] #(n/times a %)))

        (instance? Composition transformation)
        (reduce transform vector (:sequence transformation)))))
  Point
  (transform [point transformation]
    (update-in point [:z] (transform-fn transformation)))

  Complex-Turtle
  (transform [turtle transformation]
    (-> turtle
        (update-in [:position] #(transform % transformation))
        (update-in [:heading] #(transform % transformation))
        (update-in [:orientation] toggle-orientation)))
  Number
  (transform [number transformation]
    (let [z (n/c [number 0])
          f (transform-fn transformation)
          w (f z)]
      (n/length w))))

(extend-protocol Turtle
  Complex-Turtle
  (move [{position :position heading :heading :as turtle} d]
    (update-in turtle [:position]
               #(transform % (->Translation
                              (n/add (:z position)
                                     (n/times (:z heading) d))))))
  (turn [turtle a]
    (update-in turtle [:heading]
               #(transform % (->Rotation a))))
  (resize [turtle r]
    (update-in turtle [:heading]
               #(transform % (->Dilation r))))
  (reflect [turtle]
    (update-in turtle [:orientation]
               toggle-orientation)))

(defn display-turtle
  [complex-turtle]
  (let [position (get-in complex-turtle [:position :z])
        heading (get-in complex-turtle [:heading :z])]
    {:position (n/coords position)
     :heading {:length (n/length heading)
               :angle ((comp n/rad->deg n/arg) heading)}
     :orientation (:orientation complex-turtle)}))

(comment
  (require '[hello-devcards.protocols] :reload)
  (in-ns 'hello-devcards.protocols)
  (use 'clojure.repl)

  (display-turtle initial-turtle)
  ;;=> {:position [0 0], :heading {:length 1.0, :angle 0.0}, :orientation :counter-clockwise}

  (apply mobius indentity-quintuple)

  (inverse (->Translation n/one))
  (transform (->Point n/zero) (->Translation n/one))
  (transform initial-turtle (->Translation n/one))

  (display-turtle (move initial-turtle 1))
  (display-turtle (turn initial-turtle 15))
  (display-turtle (resize initial-turtle 2))
  (display-turtle (reflect initial-turtle))

  (display-turtle
   (-> initial-turtle
       (move 10)
       (turn 45)
       (resize 2)
       reflect))
  ;;=> {:position [10.0 0.0], :heading {:length 2.0, :angle 45.0}, :orientation :clockwise}

  )
