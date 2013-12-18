(ns ^{:clojure.tools.namespace.repl/load false} frodo.web
    (:require [clojure.tools.namespace.repl :refer [refresh]]
              [clojure.string :as s]
              [org.httpkit.server :refer [run-server] :rename {run-server start-httpkit!}]))

(defn handler-deprecation-warning! []
  (binding [*out* *err*]
    (println (s/join "\n" ["WARN: Frodo now expects a :handler-fn, rather than a :handler key."
                           "This is to support Stuart Sierra's reloaded workflow."
                           "Please see https://github.com/james-henderson/frodo for more information."
                           "The original behaviour will be removed in v0.3.0."]))))

(defn resolve-sym [sym]
  (when-let [sym-ns (namespace sym)]
    (require (symbol sym-ns)))
  (ns-resolve *ns* sym))

(defn get-handler [config]
  (let [handler-fn (or (some-> (get-in config [:frodo/config :web :handler-fn])
                               resolve-sym)
                       (when-let [handler (or (get-in config [:frodo/config :web :handler])
                                              (:handler config))]
                         (handler-deprecation-warning!)
                         (constantly (resolve-sym handler))))]
    (handler-fn)))

(defn read-web-config [config]
  (let [handler (get-handler config)
        web-port (get-in config [:frodo/config :web :port])]
    (assert handler "Please supply a handler in the Frodo config")
    (assert handler "Please supply a web server port in the Frodo config")

    {:handler handler
     :web-port web-port}))

(defn- start-web-server! [!server config]
  (let [{:keys [handler web-port]} (read-web-config config)
        _ (println "Starting web server, port" web-port)
        server (start-httpkit! handler {:port web-port :join? false})]
    (dosync
     (ref-set !server server)
     nil)))

(defn- stop-web-server! [!server]
  (when-let [server (dosync
                     (let [server @!server]
                       (ref-set !server nil)
                       server))]
    (println "Stopping web server.")
    ;; calling server kills it
    (server)
    nil))

(defn- reload-web-server! [!server config]
  (stop-web-server! !server)
  (refresh)
  (start-web-server! !server config))

(defn init-web! [_config]
  (let [!server (ref nil)]
    (intern 'user 'start-frodo! #(start-web-server! !server (_config)))
    (intern 'user 'stop-frodo! #(stop-web-server! !server))
    (intern 'user 'reload-frodo! #(reload-web-server! !server (_config)))

    (start-web-server! !server (_config))))
