(ns ^{:clojure.tools.namespace.repl/load false} frodo.core
    (:require [nomad :refer [defconfig]]
              [frodo.nrepl :refer [start-nrepl!]]
              [frodo.web :refer [init-web!]]
              [clojure.tools.namespace.repl :refer [refresh]]
              [clojure.string :as s]
              [org.httpkit.server :refer [run-server] :rename {run-server start-httpkit!}]))

(defn init-frodo [config-resource & [{:keys [cljx? target-path] :as nrepl-opts}]]
  (defconfig ^:private _config config-resource)

  (start-nrepl! (_config) nrepl-opts)

  (init-web! _config))
