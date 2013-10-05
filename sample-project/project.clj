(defproject sample-project "no-version"

  :dependencies [[org.clojure/clojure "1.5.1"]
                 [ring/ring-core "1.2.0"]]

  :plugins [[jarohen/lein-frodo "0.1.3-SNAPSHOT"]]

  :frodo/config-resource "config.edn")
