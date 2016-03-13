(ns hello-devcards.pencil
  (:require
   [clojure.core.async :as async]
   [complex.number :as n]))

(comment
  (require '[hello-devcards.pencil] :reload)
  (in-ns 'hello-devcards.pencil)
  (use 'clojure.repl)
  )


;; mult
(def to-mult (async/chan 1))

(def m (async/mult to-mult))

(comment

  (let [c (async/chan 1)]
    (async/tap m c)
    (async/go (loop []
                (when-let [v (async/<! c)]
                  (println "Got! " v)
                  (recur)))))

  (async/>!! to-mult 42)
  (async/>!! to-mult 43)
  (async/close! to-mult)
  )
;; system tick
;; a channel to send out ticks
;; a return channel to collect tocks
;; tick tock goes the clock
;; once around in 60 ticks
;; pub sub

(def ticker-chan (async/chan 1))

(def ticker-mult (async/mult ticker-chan))

(defn send-n-ticks [n ch]
  (async/go-loop [i 0]
    (println "waiting ....")
    (async/<! (async/timeout 1000))
    (println (str "sending a system tick: " i))
    (async/>! ch [:tick i])
    (println "tick sent")
    (if (< i n)
      (recur (inc i)))))

(defn process-ticks [ch]
  (async/go
    (loop []
      (if-let [t (async/<! ch)]
        (do (println t)
            (recur))
        (println :done)))))

(comment
  (let [c (async/chan 1)]
    (async/tap ticker-mult c)
    (process-ticks c)
    (async/untap ticker-mult c))

  (send-n-ticks 10 ticker-chan)

  )

(defn sqawn-linear-turtle [inital-turtle ]
  )

;; parameterized semi circle
(defn p [n]
  (let [d (+ (* n n) 1)]
    [(/ (* 2 n) d) (/ (- (* n n) 1) d)]))

(defn sq-sum [[x y]]
  (+ (* x x) (* y y)))

(comment
  (for [n (range 1 11)]
    [n (p n) (p (/ n)) (sq-sum (p n)) (sq-sum (p (/ n)))])

  (for [n (range 2 101)]
    (p n))
  )

(defn trip [n]
  [(* 2 n) (- (* n n) 1) (+ (* n n) 1)])

(defn py-trip? [a b c]
  (= (+ (* a a) (* b b)) (* c c)))

(comment
  (map trip (range 1 11))
  ;;=> ([2 0 2] [4 3 5] [6 8 10] [8 15 17] [10 24 26] [12 35 37] [14 48 50] [16 63 65] [18 80 82] [20 99 101])
  (map #(apply py-trip? %) (map trip (range 1 11)))
  (every? true? (map #(apply py-trip? %) (map trip (range 1 11))))
  ;;=> true
  )
