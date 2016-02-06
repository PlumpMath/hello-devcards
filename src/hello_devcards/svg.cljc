(ns hello-devcards.svg)

(defn line [p1 p2]
  (let [[x1 y1] p1
        [x2 y2] p2]
    [:line {:x1 x1 :y1 y1 :x2 x2 :y2 y2}]))

(defn circle [center radius]
  (let [[cx cy] center]
    [:circle {:cx cx :cy cy :r radius}]))

(defn path [path-string]
  [:path {:d path-string}])

(defn defs [& paths]
  (into [:defs] paths))

(defn transform-str [[x y] angle scale]
  (str "translate(" x "," y ") "
       "rotate(" angle ") "
       "scale(" scale ") "))

(defn use-path [id transform]
  [:use {:xlink-href id
         :transform transform
         :stroke "black"
         :fill "transparent"}])

(comment
  (require '[hello-devcards.svg] :reload)
  (in-ns 'hello-devcards.svg)

  (use-path "turtle-shell" (transform-str [0 0] 0 1))
  ;;=> [:use {:xlink-href "turtle-shell", :transform "translate(0,0) rotate(0) scale(1) "}]
  )

;; svg path components
(defrecord M [point])
(defrecord m [vector])
(defrecord L [point])
(defrecord l [vector])
(defrecord Q [c-point point])
(defrecord q [c-vector vector])

(defprotocol Path
  (as->str [path-component]))

(defn point->str [[x y]]
  (str x " " y " "))

(extend-protocol Path
  M
  (as->str [{point :point}]
    (str "M " (point->str point)))
  m
  (as->str [{vector :vector}]
    (str "m " (point->str vector)))
  L
  (as->str [{point :point}]
    (str "L " (point->str point)))
  l
  (as->str [{vector :vector}]
    (str "l " (point->str vector)))
  Q
  (as->str [{c-point :c-point point :point}]
    (str "Q " (point->str c-point) " " (point->str point)))
  q
  (as->str [{c-vector :c-vector vector :vector}]
    (str "q " (point->str c-vector) " " (point->str vector))))

(defn reduce-path [& path-components]
  (clojure.string/join (map as->str path-components)))

(defn group-svg [id & elements]
  (into [:g {:id id}] elements))

(def origin [0 0])

(defn straight-arrow [id l]
  (let [q (* (/ 4) l)
        tip [l 0]
        v1 [(- q) q]
        v2 [0 (+ (- q) (- q))]
        v3 [q q]]
    (group-svg id
               (circle origin 3)
               (path (reduce-path (->M origin)
                                  (->l tip)
                                  (->l v1)
                                  (->l v2)
                                  (->l v3))))))

(def test-arrow
  [:g {:id "pixie"}
   [:line {:x1 0 :y1 0 :x2 30 :y2 0}]
   [:circle {:cx 0 :cy 0 :r 3 :stroke "green" :fill "blue"}]
   [:path {:d "M 40 0 Q 30 0 30 10 Q 33 5 30 0"}]
   [:path {:d "M 40 0 Q 30 0 30 -10 Q 33 -5 30 0"}]])

(comment
  (->M [0 0])
  (straight-arrow "test" 8)
  ;;=> [:g {:id "test"} [:circle {:cx 0, :cy 0, :r 3}] [:path {:d "M 0 0l 6 0l 0 2l 8 0l 0 -2l 6 0"}]]
  )
