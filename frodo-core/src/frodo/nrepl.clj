(ns ^{:clojure.tools.namespace.repl/load false} frodo.nrepl
  (:require [clojure.tools.nrepl.server :refer [start-server]]))

(comment
  (defn cljs-repl? [config]
    (boolean (get-in config [:nrepl :cljs-repl?])))

  (defn repl-handler-form [project config]
    `(clojure.tools.nrepl.server/default-handler
       ~@(concat (when (:cljx project)
                   `[#'cljx.repl-middleware/wrap-cljx])
                 (when (cljs-repl? config)
                   `[#'cemerick.piggieback/wrap-cljs-repl]))))

  (defn run-nrepl-form [project config]
    (when-let [nrepl-port (get-in config [:nrepl :port])]
      `(do
         (doto (io/file "target/repl-port")
           (spit ~nrepl-port)
           (.deleteOnExit))
         (clojure.tools.nrepl.server/start-server :port ~nrepl-port
                                                  :handler ~(repl-handler-form project config))
         (println "Started nREPL server, port" ~nrepl-port)))))

(defn start-nrepl! [config & [{:keys [cljx? target-path]}]]
  )

