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
