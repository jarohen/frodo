(ns ^{:clojure.tools.namespace.repl/load false
      :clojure.tools.namespace.repl/unload false}
  frodo.core
  (:require [nomad :refer [defconfig]]
            [frodo.nrepl :refer [start-nrepl!]]
            [frodo.web :refer [init-web!]]))

(defn init-frodo! [{:keys [config-resource repl-options target-path] :as project}]
  (defconfig ^:private _config config-resource)

  (start-nrepl! (_config) project)

  (init-web! _config))
