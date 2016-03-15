(ns hello-devcards.pencils
  (:require
   [devcards.core]
   [complex.number :as n :refer [zero one i negative-one negative-i infinity add sub mult div]]
   [complex.vector :as v]
   [hello-devcards.complex :as turtle]
   [hello-devcards.polygon :as polygon]
   [hello-devcards.lattice :as lattice]
   [hello-devcards.utils :as u]
   [hello-devcards.mappings :as mappings]
   [hello-devcards.svg :as svg]
   [hello-devcards.geometry :as g]
   [reagent.core :as reagent]
   [sablono.core :as sab :include-macros true]
   [goog.events :as events]
   [cljs.core.async :as async :refer [>! <! put! chan alts! timeout]])
  (:require-macros
   [devcards.core :as dc :refer [defcard deftest defcard-rg defcard-doc]]
   [cljs.core.async.macros :refer [go]])
  (:import [goog.events EventType]))

(defcard story
  "
# Pulsating Pencils

standard turtle, eigth mapping

state consists of

* lattice
* vertical lines parallel to heading
* horizontal lines perpendicular to heading
* concentric circles about position
* radial lines through position
")
