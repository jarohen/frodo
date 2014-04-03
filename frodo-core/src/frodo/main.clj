(ns ^{:clojure.tools.namespace.repl/load false
      :clojure.tools.namespace.repl/unload false}
  frodo.main
  (:gen-class)
  (:require [clojure.java.io :as io]
            [nomad :refer [defconfig]]
            [frodo.core :refer [init-frodo!]])
  (:import [java.util.jar Manifest]))

(defn frodo-config-location []
  (slurp (io/resource "META-INF/frodo-config-resource")))

(defn repl-options []
  (read-string (slurp (io/resource "META-INF/frodo-repl-options"))))

(defn -main [& [config-file & args]]
  (let [config-resource (or (when config-file
                              (io/file config-file))
                            (io/resource (frodo-config-location)))]
    (init-frodo! {:config-resource config-resource
                  :repl-options (repl-options)})))
