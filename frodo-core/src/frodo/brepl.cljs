(ns frodo.brepl
  (:require [weasel.repl :as ws-repl]))

(when-let [repl-port js/frodo_repl_port]
  (ws-repl/connect (str "ws://localhost:" repl-port)))

