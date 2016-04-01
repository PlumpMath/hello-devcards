(ns hello-devcards.polygon
  (:require
   [hello-devcards.complex :as turtle]))

(def initial-app-state
  {:turtle turtle/initial-turtle
   :points []
   :polygons []
   :current-color "grey"})

(defn transform-state
  "transform state using f, a function of complex number
  from user space into screen coordinates"
  [f state]
  (-> state
      (update-in [:turtle] #(turtle/transform-turtle f %))
      (update-in [:points] #(mapv f %))
      (update-in [:polygons]
                 #(mapv
                   (fn [poly]
                     (-> poly
                         (update-in [:points]
                          (fn [points] (mapv f points)))))
                   %))))

;; ten commands
(defrecord BeginPoly [])
(defrecord Point [])
(defrecord ClosePoly [])
(defrecord CloseSection [])
(defrecord Forward [d])
(defrecord Turn [a])
(defrecord Resize [s])
(defrecord In [])
(defrecord Out [])
(defrecord Pause [delay])

(defn poly
  "turtle program for drawing a regular polygon"
  [n]
  (let [a (/ 360 n)]
    (flatten
     (list
      (->BeginPoly)
      (repeat n
              (list (->Turn a)
                    (->Point)
                    (->Pause 100)))
      (->ClosePoly)))))

(defn section
  "an n-fold section"
  [n]
  (let [a (/ 360 n)]
    (list
     (->BeginPoly)
     (->Turn a)
     (->Point)
     (->Pause 100)
     (->Out)
     (->Point)
     (->Pause 100)
     (->Turn (- a))
     (->Point)
     (->Pause 100)
     (->In)
     (->Point)
     (->Pause 100)
     (->CloseSection))))

(defn section-no-delay
  "an n-fold section"
  [n]
  (let [a (/ 360 n)]
    (list
     (->BeginPoly)
     (->Turn a)
     (->Point)
     (->Out)
     (->Point)
     (->Turn (- a))
     (->Point)
     (->In)
     (->Point)
     (->CloseSection))))

(defn wheel [n]
  "an n-fold wheel"
  (let [a (/ 360 n)]
    (flatten
     (repeat n
             (list (section-no-delay n)
                   (->Turn a)
                   (->Pause 10))))))

(defn full-wheel [n]
  "an n-fold wheel"
  (let [a (/ 360 n)]
    (flatten (list
              (->In)
              (wheel n)
              (repeat 5
                      (list
                       (->Out)
                       (wheel n)))
              (repeat 4 (->In))))))

(defn start-poly [state]
  (let [turtle (:turtle state)]
    (-> state
        (assoc-in [:points]
                  [(turtle/tip turtle)]))))

(defn add-point [state]
  (let [tip (turtle/tip (:turtle state))]
    (-> state
        (update-in [:points]
                   #(conj % tip)))))

(defn close-poly [state]
  (-> state
      (update-in [:polygons]
                 #(conj % {:class-name "polygon"
                           :color (:current-color state)
                           :points (drop-last (:points state))}))
      (assoc-in [:points] [])))

(defn close-section
  "color determined by the state of the tutle"
  [state]
  (-> state
      (update-in [:polygons]
                 #(conj % {:color (turtle/color (:turtle state))
                           :class-name "section"
                           :points (drop-last (:points state))}))
      (assoc-in [:points] [])))

(defprotocol CommandProcessor
  (process-command [command state]))

(extend-protocol CommandProcessor
  Forward
  (process-command [{d :d} state]
    (update-in state [:turtle] #(turtle/move % d)))
  Turn
  (process-command [{a :a} state]
    (update-in state [:turtle] #(turtle/turn % a)))
  Resize
  (process-command [{s :s} state]
    (update-in state [:turtle] #(turtle/resize % s)))
  In
  (process-command [_ state]
    (update-in state [:turtle] turtle/in))
  Out
  (process-command [_ state]
    (update-in state [:turtle] turtle/out))
  BeginPoly
  (process-command [_ state]
    (start-poly state))
  Point
  (process-command [_ state]
    (add-point state))
  ClosePoly
  (process-command [_ state]
    (close-poly state))
  CloseSection
  (process-command [_ state]
    (close-section state))
  Pause
  (process-command [_ state] state))

(comment
  (in-ns 'hello-devcards.polygon)
  initial-app-state
  (start-poly initial-app-state)

  (-> initial-app-state start-poly add-point)
  (-> initial-app-state
      start-poly
      add-point
      close-poly)

  (reduce
   (fn [state command]
     (process-command command state))
   initial-app-state
   (poly 4))

  (map (comp count :points)
       (reductions
        (fn [state command]
          (process-command command state))
        initial-app-state
        (poly 4)))
  )
