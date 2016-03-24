(ns pages.core
  (:require
   [devcards.core]
   [complex.number :as n :refer [zero one i negative-one negative-i infinity add sub mult div]]
   [hello-devcards.bezier]
   [hello-devcards.pixie-turtle]
   [hello-devcards.chessboard]
   [hello-devcards.color-wheel]
   [hello-devcards.colored-polygon]
   [hello-devcards.transforms]
   [hello-devcards.projective-turtle]
   [hello-devcards.sierpensky])
  (:require-macros
   [devcards.core :as dc :refer [defcard deftest defcard-rg defcard-doc]]))

(devcards.core/start-devcard-ui!)
