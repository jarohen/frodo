(ns ^{:clojure.tools.namespace.repl/load false} frodo.nrepl
    (:require [clojure.tools.nrepl.server :as nrepl]
              [clojure.java.io :as io]
              [alembic.still :as a]
              [frodo.brepl :as brepl]
              [cemerick.piggieback :as p]))

(defn repl-handler [{:keys [brepl-port nrepl-middleware]}]
  (apply nrepl/default-handler
         (set (cond-> nrepl-middleware
                brepl-port (conj #'p/wrap-cljs-repl)))))

(defn start-nrepl! [config & [{:keys [repl-options target-path]} :as project]]
  (when-let [nrepl-port (get-in config [:frodo/config :nrepl :port])]
    (when target-path
      (doto (io/file target-path "repl-port")
        (spit nrepl-port)
        (.deleteOnExit)))

    (let [brepl-port (get-in config [:frodo/config :nrepl :brepl-port])]
      (when brepl-port
        (brepl/load-brepl! brepl-port))
      
      (nrepl/start-server :port nrepl-port
                          :handler (repl-handler (assoc repl-options
                                                   :brepl-port brepl-port)))
      
      (println "Started nREPL server, port" nrepl-port))))
