(defproject hello-devcards "0.1.0-SNAPSHOT"
  :description "Hello Devcards"
  :url "http://wbabic.github.io/hello-devcards"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.9.0-alpha14"]
                 [org.clojure/clojurescript "1.9.473"]

                 [cljsjs/react "15.4.2-2"]
                 [cljsjs/react-dom "15.4.2-2"]
                 [cljsjs/react-dom-server "15.4.2-2"]
                 [devcards "0.2.2" :exclusions [org.clojure/tools.reader
                                                cljs/react
                                                cljsjs/react-dom-server]]
                 [sablono "0.7.7"]
                 [reagent "0.6.0"]

                 [org.clojure/core.match "0.3.0-alpha4"]
                 [org.clojure/core.async "0.3.441"]

                 [complex/complex "0.1.9"]]

  :plugins [[lein-cljsbuild "1.1.4"]
            [lein-figwheel "0.5.7"
             :exclusions [org.clojure/clojure
                          ring/ring-core joda-time
                          org.clojure/tools.reader]]]

  :clean-targets ^{:protect false} ["resources/public/js/compiled"
                                    "resources/public/js/pages"
                                    "target"]

  :source-paths ["src"]

  :cljsbuild {
              :builds [{:id "devcards"
                        :source-paths ["src"]
                        :figwheel { :devcards true } ;; <- note this
                        :compiler { :main       "hello-devcards.core"
                                    :asset-path "js/compiled/devcards_out"
                                    :output-to  "resources/public/js/compiled/hello_devcards_devcards.js"
                                    :output-dir "resources/public/js/compiled/devcards_out"
                                    :source-map-timestamp true }}
                       {:id "pages"
                        :source-paths ["src" "pages-src"]
                        :compiler {:main       "pages.core"
                                   :devcards true
                                   :asset-path "js/pages/out"
                                   :output-to  "resources/public/js/pages/devcards.js"
                                   :optimizations :advanced}}]}

  :figwheel {:css-dirs ["resources/public/css"]
             :server-port 3451})
