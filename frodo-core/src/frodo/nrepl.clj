(ns ^{:clojure.tools.namespace.repl/load false} frodo.nrepl
    (:require [clojure.tools.nrepl.server :as nrepl]
              [clojure.java.io :as io]
              [alembic.still :as a]))

(defn- load-cljx! []
  (with-out-str
    (a/distill '[[com.keminglabs/cljx "0.3.2"]])
    (require 'cljx.repl-middleware)))

(defn- load-cljs-repl! []
  (with-out-str
    (a/distill '[[com.cemerick/austin "0.1.3"]])
    (require 'cemerick.piggieback)
    (require 'cemerick.austin.repls)))

(defn- repl-handler [config cljx?]
  (apply nrepl/default-handler 
         (concat (when cljx?
                   (load-cljx!)
                   (eval `[#'cljx.repl-middleware/wrap-cljx]))
                 (when (get-in config [:frodo/config :nrepl :cljs-repl?])
                   (load-cljs-repl!)
                   (eval `[#'cemerick.piggieback/wrap-cljs-repl])))))

(defn start-nrepl! [config & [{:keys [cljx? target-path]}]]
  (when-let [nrepl-port (get-in config [:frodo/config :nrepl :port])]
    (when target-path
      (doto (io/file target-path "repl-port")
        (spit nrepl-port)
        (.deleteOnExit)))
    
    (nrepl/start-server :port nrepl-port :handler (repl-handler config cljx?))
    (println "Started nREPL server, port" nrepl-port)))

