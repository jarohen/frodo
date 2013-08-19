(defproject sample-project "no-version"

  :dependencies [[org.clojure/clojure "1.5.1"]
                 [ring "1.2.0"]]

  :plugins [[jarohen/nomad-ring "0.1.0-SNAPSHOT"]]

  :nomad-ring/resource "config.edn")
