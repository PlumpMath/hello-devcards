(ns hello-devcards.core
  (:require
   [devcards.core]
   [complex.number :as n :refer [zero one i negative-one negative-i infinity add sub mult div]]
   [hello-devcards.circle]
   [hello-devcards.bezier]
   [hello-devcards.pixie-turtle]
   [hello-devcards.chessboard]
   [hello-devcards.color-wheel]
   [hello-devcards.colored-polygon]
   [hello-devcards.transforms]
   [hello-devcards.pencils]
   [hello-devcards.projective-turtle]
   [hello-devcards.sierpensky]
   [hello-devcards.control-panel]
   [reagent.core :as reagent]
   [sablono.core :as sab :include-macros true])
  (:require-macros
   [reagent.ratom :as ratom :refer [reaction]]
   [devcards.core :as dc :refer [defcard deftest defcard-rg defcard-doc]]))

(enable-console-print!)

(defn main []
  ;; conditionally start the app based on wether the #main-app-area
  ;; node is on the page
  (if-let [node (.getElementById js/document "main-app-area")]
    (js/React.render (sab/html [:div "This is working"]) node)))

(main)
