(ns hello-devcards.lattice
  "a complex lattice turtle in two dimensions"
  (:require
   [complex.number :as n]))

(def initial-lattice-turtle
  {:position n/zero
   :heading1 n/one
   :heading2 n/i})

(def initial-state
  {:turtle initial-lattice-turtle
   :points []})

;; just three commands
(defrecord Point [])
(defrecord Forward1 [d])
(defrecord Forward2 [d])

(defprotocol CommandProcessor
  (process-command [command state]))

(defn add-point [state]
  (update-in state [:points] #(conj % (get-in state [:turtle :position]))))

(defn move1
  "move given turtle by d units in direction of heading1"
  [state d]
  (update-in state [:turtle :position]
             #(n/add % (n/times (get-in state [:turtle :heading1]) d))))

(defn move2
  "move given turtle by d units in direction of heading2"
  [state d]
  (update-in state [:turtle :position]
             #(n/add % (n/times (get-in state [:turtle :heading2]) d))))

(extend-protocol CommandProcessor
  Point
  (process-command [_ state] (add-point state))
  Forward1
  (process-command [{d :d} state] (move1 state d))
  Forward2
  (process-command [{d :d} state] (move2 state d)))

(defn forward1
  "add n points by moving forward along heading 1"
  [n]
  (flatten
   (repeat n
           (list
            (->Forward1 1)
            (->Point)))))

(defn backward1
  "add n points by moving forward along heading 1"
  [n]
  (flatten
   (repeat n
           (list
            (->Forward1 -1)
            (->Point)))))

(defn reducing-fn [state command]
  (process-command command state))

(defn run-program [state program]
  (reduce
   reducing-fn
   state
   program))

(defn make-line1
  "make lattice line along heading1 consisting of n steps forward and backward
the returned turtle position is that same as in the given state
  the position of the turtle of the returned state
  is the same as that of the given state"
  [state n]
  (let [initial-position (get-in state [:turtle :position])]
    (-> state
        (run-program (flatten (list (->Point) (forward1 n))))
        (assoc-in [:turtle :position] initial-position)
        (run-program (backward1 n))
        (assoc-in [:turtle :position] initial-position))))

(defn four-by-four-lattice
  "make a lattice consisting of 4 setps in each of the four directions"
  [state]
  (let [initial-position (get-in state [:turtle :position])]
   (-> state
       (make-line1 4)

       (move2 1)
       (make-line1 4)
       (move2 -2)
       (make-line1 4)
       (assoc-in [:turtle :position] initial-position)

       (move2 2)
       (make-line1 4)
       (assoc-in [:turtle :position] initial-position)
       (move2 -2)
       (make-line1 4)
       (assoc-in [:turtle :position] initial-position)

       (move2 3)
       (make-line1 4)
       (assoc-in [:turtle :position] initial-position)
       (move2 -3)
       (make-line1 4)
       (assoc-in [:turtle :position] initial-position)

       (move2 4)
       (make-line1 4)
       (assoc-in [:turtle :position] initial-position)
       (move2 -4)
       (make-line1 4)
       (assoc-in [:turtle :position] initial-position))))

(comment
  (require '[hello-devcards.lattice] :reload)
  (in-ns 'hello-devcards.lattice)
  (process-command (->Forward1 1) initial-state)
  (process-command (->Forward1 -1) initial-state)
  (process-command (->Forward2 1) initial-state)
  (process-command (->Forward2 -1) initial-state)
  (process-command (->Point) initial-state)
  (:points (make-line1 initial-state 4))
  (:points (four-by-four-lattice initial-state))
  )
