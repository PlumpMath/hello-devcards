(ns hello-devcards.pencil
  (:require
   [clojure.core.async :as async]
   [complex.number :as n]))

(comment
  (require '[hello-devcards.pencil] :reload)
  (in-ns 'hello-devcards.pencil)
  (use 'clojure.repl)
  )

;; system tick
;; a channel to send out ticks

(def ticker-chan (async/chan 1))

;; (def ticker-mult (async/mult ticker-chan))

(defn send-n-ticks [n ch]
  (async/go-loop [i 0]
    (println "waiting for timeout")
    (async/<! (async/timeout 1000))
    (println (str "waiting to send system tick: " i))
    (async/>! ch [:tick i])
    (println "tick " i " sent")
    (if (< i n)
      (recur (inc i)))))

(defn process-ticks [ch]
  (async/go
    (loop []
      (if-let [t (async/<! ch)]
        (do (println t)
            (recur))
        (println :done)))))

(defn back-pressure [tick-chan]
  (let [bp (async/chan)]
    (async/go (loop []
          (when-let [tick (async/<! tick-chan)]
            (async/>! bp :tock)
            (recur))))
    bp))

(comment
  (def ret-ch (back-pressure ticker-chan))
  (send-n-ticks 10 ticker-chan)
  (async/take! ret-ch #(println "ret-ch: " %))
  )

(comment
  (let [c (async/chan 1)]
    (async/tap ticker-mult c)
    (process-ticks c))

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
