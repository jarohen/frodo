(ns ^{:clojure.tools.namespace.repl/load false} frodo.core
    (:require [nomad :refer [defconfig]]
              [frodo.nrepl :refer [start-nrepl!]]
              [frodo.web :refer [init-web!]]))

(defn init-frodo! [config-resource & [{:keys [cljx? target-path] :as nrepl-opts}]]
  (defconfig ^:private _config config-resource)
  (prn (_config))
  (start-nrepl! (_config) nrepl-opts)

  (init-web! _config))
