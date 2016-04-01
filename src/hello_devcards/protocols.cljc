(ns hello-devcards.protocols
  (:require
   [complex.number :as n]))

(defrecord Point [z])
(defrecord Vector [z])
(defrecord Orientation [keyword])

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
   (->Complex-Turtle position heading (->Orientation :counter-clockwise))))

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
  (transform-fn [reflection]
    (fn [z] (n/conjugate z))))

(defrecord Dilation [ratio]
  Transform
  (inverse [{:keys [ratio]}] (->Dilation (/ ratio)))
  (transform-fn [{:keys [ratio]}]
    (fn [z] (n/times z ratio))))

(defrecord Rotation [angle]
  Transform
  (inverse [rotation] (->Rotation (- (:angle rotation))))
  (transform-fn [rotation]
    (fn [z] (n/times (n/complex-polar (:angle rotation)) z))))

(defrecord Translation [v]
  Transform
  (inverse [translation] (->Translation (n/minus v)))
  (transform-fn [translation]
    (fn [z] (n/add z (:v translation)))))

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
    (apply comp (map transform-fn sequence))))

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
        ;; translation does not effect vectors
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
        (update-in [:orientation] #(transform % transformation))))
  Number
  (transform [number transformation]
    (let [z (n/c [number 0])
          f (transform-fn transformation)
          w (f z)]
      (n/length w)))
  Orientation
  (transform [orientation transformation]
    (cond
      (instance? Reflection transformation)
      (update-in orientation [:keyword] toggle-orientation)

      (instance? Composition transformation)
      (reduce
       (fn [orien trans]
         (transform orien trans))
       orientation
       (:sequence transformation))

      :else
      orientation)))

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
     :orientation (get-in complex-turtle [:orientation :keyword])}))

(comment
  (require '[hello-devcards.protocols] :reload)
  (in-ns 'hello-devcards.protocols)
  (use 'clojure.repl)

  (display-turtle initial-turtle)
  ;;=> {:position [0 0], :heading {:length 1.0, :angle 0.0}, :orientation :counter-clockwise}

  (apply mobius indentity-quintuple)

  ;; transform-fn of a Transform
  (n/coords ((transform-fn (->Rotation 45)) n/one))
  ;;=> [0.7071067811865476 0.7071067811865475]
  (n/coords ((transform-fn (->Composition
                            (list (->Reflection) (->Rotation 45))))
             n/one))
  ;;=> [0.7071067811865476 -0.7071067811865475]

  ;; inverse of a transform is a transform
  (inverse (->Translation n/one))

  ;; transform a point
  (transform (->Point n/zero) (->Translation n/one))

  ;; transform a turtle
  (display-turtle (transform initial-turtle (->Translation n/one)))
  ;;=> {:position [1 0], :heading {:length 1.0, :angle 0.0}, :orientation :counter-clockwise}
  (display-turtle (transform initial-turtle (->Reflection)))
  ;;=> {:position [0 0], :heading {:length 1.0, :angle -0.0}, :orientation :clockwise}
  (let [t (->Composition (list (->Rotation 45) (->Reflection)))]
    (-> initial-turtle
        (transform t)
        display-turtle))
  ;;=> {:position [0 0], :heading {:length 1.0, :angle 315.0}, :orientation :clockwise}

  ;; transform a vector
  (let [t (->Composition (list (->Rotation 45) (->Reflection)))]
    (-> (complex-vector 1 0)
        (transform t)
        :z
        n/coords))
  ;;=> [0.7071067811865476 -0.7071067811865475]
  ;; a vector is not effected by translation
  (let [t (->Composition (list (->Rotation 45) (->Reflection) (->Translation n/one)))]
    (-> (complex-vector 1 0)
        (transform t)
        :z
        n/coords))
  ;;=> [0.7071067811865476 -0.7071067811865475]

  (let [t (->Composition (list (->Rotation 45) (->Reflection)))]
    (-> (->Orientation :clockwise)
        (transform t)
        :keyword))
  ;;=> :counter-clockwise
  (let [t (->Composition (list (->Reflection) (->Rotation 45) (->Reflection)))]
    (-> (->Orientation :clockwise)
        (transform t)
        :keyword))
  ;;=> :clockwise

  ;; transform a point
  (let [t (->Composition (list (->Reflection) (->Rotation 45) (->Reflection)))]
    (-> (point n/one)
        (transform t)
        :z
        n/coords))
  ;;=>

  (display-turtle (transform initial-turtle (->Composition (list (->Rotation 45) (->Reflection)))))

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
