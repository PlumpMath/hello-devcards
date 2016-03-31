(defproject hello-devcards "0.1.0-SNAPSHOT"
  :description "Hello Devcards"
  :url "http://wbabic.github.io/hello-devcards"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.8.40"]
                 [org.clojure/tools.reader "1.0.0-alpha3"]
                 [org.clojure/tools.analyzer.jvm "0.6.9"]

                 [devcards "0.2.1-6" :exclusions [org.clojure/tools.reader]]
                 [sablono "0.6.3"]
                 [reagent "0.6.0-alpha"]

                 [org.clojure/core.match "0.3.0-alpha4"]
                 [org.clojure/core.async "0.2.374"]

                 [complex/complex "0.1.9"]

                 [ring/ring-core "1.4.0"]
                 [clj-time "0.9.0"]

                 [fipp "0.6.4"]
                 ]

  :plugins [[lein-cljsbuild "1.1.3"]
            [lein-figwheel "0.5.2"
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

  :figwheel { :css-dirs ["resources/public/css"] })
