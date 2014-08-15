(ns ^{:clojure.tools.namespace.repl/load false
      :clojure.tools.namespace.repl/unload false}
  frodo.core
  (:require [nomad :refer [read-config]]
            [frodo.nrepl :refer [start-nrepl!]]
            [frodo.web :refer [init-web!]]))

(defn init-frodo! [{:keys [config-resource repl-options target-path] :as project}]
  (letfn [(load-config []
            (read-config config-resource))]
    
    (start-nrepl! (load-config) project)

    (init-web! load-config)))
