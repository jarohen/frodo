(ns ^{:clojure.tools.namespace.repl/load false
      :clojure.tools.namespace.repl/unload false}
  frodo.brepl
  (:require [cemerick.piggieback :as p]
            [weasel.repl.websocket :as ws-repl]))

(def ^:private brepl-port)

(defn brepl []
  (p/cljs-repl :repl-env (ws-repl/repl-env :port brepl-port)))

(defn brepl-open? []
  ;; Admittedly, this is quite a hack to decide whether the WS is
  ;; open...
  (boolean @@#'ws-repl/repl-out))

(defn brepl-js []
  (format "window.frodo_repl_port=%d;"
          (when (brepl-open?)
            brepl-port)))




