(ns ^{:clojure.tools.namespace.repl/load false} frodo.nrepl
    (:require [clojure.tools.nrepl.server :as nrepl]
              [clojure.java.io :as io]
              [alembic.still :as a]))

(defn- load-cljx! []
  (with-out-str
    (a/distill '[[com.keminglabs/cljx "0.3.2"]])
    (require 'cljx.repl-middleware)))

(defn- load-brepl! [brepl-port]
  (with-out-str
    (a/distill '[[weasel "0.1.0"]
                 [com.cemerick/piggieback "0.1.3"]])
    (require 'frodo.brepl)
    
    (doto 'frodo.brepl
      (intern 'brepl-port brepl-port))
    
    (intern 'user 'frodo-brepl (eval '#'frodo.brepl/brepl))))

(defn- repl-handler [config cljx?]
  (apply nrepl/default-handler 
         (concat (when cljx?
                   (load-cljx!)
                   (eval `[#'cljx.repl-middleware/wrap-cljx]))
                 (when-let [brepl-port (get-in config [:frodo/config :nrepl :brepl-port])]
                   (load-brepl! brepl-port)
                   (eval `[#'cemerick.piggieback/wrap-cljs-repl])))))

(defn start-nrepl! [config & [{:keys [cljx? target-path]}]]
  (when-let [nrepl-port (get-in config [:frodo/config :nrepl :port])]
    (when target-path
      (doto (io/file target-path "repl-port")
        (spit nrepl-port)
        (.deleteOnExit)))
    
    (nrepl/start-server :port nrepl-port :handler (repl-handler config cljx?))
    (println "Started nREPL server, port" nrepl-port)))

