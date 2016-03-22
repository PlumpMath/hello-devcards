(ns hello-devcards.recursive-turtle
  (:refer-clojure :exclude [double])
  (:require
   [hello-devcards.polygon :as poly]
   [hello-devcards.complex :as turtle]))

;; Sierpensky gasket
;; turtle transformations
(defn poly
  ([n] (fn [turtle] {:poly turtle}))
  ([n turtle] ((poly n) turtle)))

(defn move
  ([d] (fn [turtle] [:move turtle]))
  ([d turtle] ((move d) turtle)))

(defn resize
  ([r] (fn [turtle] [[:resize (/ 2)] turtle]))
  ([r turtle] ((resize r) turtle)))

(defn turn
  ([angle] (fn [turtle] [[:turn angle] turtle]))
  ([angle turtle] ((turn angle) turtle)))

(def triangle (poly 3))
(def half     (resize (/ 2)))
(def double   (resize 2))
(def forward  (move 1))
(def turn-120 (turn 120))
(def forward-turn (comp turn-120 forward))

(def f1 half)
(def f2 (comp f1 forward-turn))
(def f3 (comp f1 forward-turn forward-turn))

(defn sierp
  "returns a list of polygons"
  [n turtle]
  (if (= n 0)
    (list (triangle turtle))
    (let [t1 (f1 turtle)
          t2 (f2 turtle)
          t3 (f3 turtle)]
      (flatten (map #(sierp (dec n) %) [t1 t2 t3])))))

(defn sierp2
  "using composition of functions"
  ([n]
   (if (= n 0)
     (list triangle)
     (let [g (fn [f] (list
                      (comp f f1)
                      (comp f f2)
                      (comp f f3)))]
       (mapcat g (sierp2 (dec n))))))
  ([n turtle]
   (map #(% :turtle) (sierp2 n))))

(comment
  (require '[hello-devcards.recursive-turtle] :reload)
  (in-ns 'hello-devcards.recursive-turtle)
  (use 'clojure.repl)
  (->> :turtle half)

  (sierp 0 :turtle)
  ({:poly :turtle})

  (sierp 1 :turtle)
  ({:poly [[:resize 1/2] :turtle]}
   {:poly [[:resize 1/2] [[:turn 120] [:move :turtle]]]}
   {:poly [[:resize 1/2] [[:turn 120] [:move [[:turn 120] [:move :turtle]]]]]})

  (first (sierp 2 :turtle))
  ;;=> {:poly [[:resize 1/2] [[:resize 1/2] :turtle]]}
  (second (sierp 2 :turtle))
  (count (sierp 2 :turtle))
  ;;=> 9

  (first (sierp2 2 :turtle))
  ;;=> {:poly [[:resize 1/2] [[:resize 1/2] :turtle]]}
)
