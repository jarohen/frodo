(ns ^{:clojure.tools.namespace.repl/load false
      :clojure.tools.namespace.repl/unload false}
  frodo.web
  (:require [clojure.tools.namespace.repl :refer [refresh]]
            [clojure.string :as s]
            [org.httpkit.server :refer [run-server] :rename {run-server start-httpkit!}]))

(defprotocol App
  (start! [_]
    "A function called when the web server starts, used to set up any
     necessary system state.

     Frodo expects this to at least have a :frodo/handler key - a Ring
     handler that will handle all web requests.

     The return value of this function is passed to the 'stop!' function
     when the web server shuts down.")
  
  (stop! [_ system]
    "A function called when the web server stops, used to tear down any
     necessary system state.

     Frodo passes it the system map that was returned by the 'start!' function.

     Its return value is ignored by Frodo."))

(defn handler-fn->app [handler-fn]
  (reify App
    (start! [_] {:frodo/handler (handler-fn)})
    (stop! [_ _])))

(defn handler->app [handler]
  (reify App
    (start! [_] {:frodo/handler handler})
    (stop! [_ _])))

(defn resolve-sym [sym]
  (when-let [sym-ns (namespace sym)]
    (require (symbol sym-ns)))
  (ns-resolve *ns* sym))

(defn get-app [config]
  (or (some-> (get-in config [:frodo/config :web :app])
              resolve-sym
              deref)
      (some-> (get-in config [:frodo/config :web :handler-fn])
              resolve-sym
              handler-fn->app)
      (some-> (get-in config [:frodo/config :web :handler])
              resolve-sym
              handler->app)))

(defn read-web-config [config]
  (let [app (get-app config)
        web-port (get-in config [:frodo/config :web :port])
        http-kit-options (get-in config [:frodo/config :web :http-kit/options])]
    (assert app "Please supply an app, or a handler in the Frodo config")
    (assert web-port "Please supply a web server port in the Frodo config")

    {:app app
     :web-port web-port
     :http-kit/options http-kit-options}))

(defn- start-instance! [!instance config]
  (let [{:keys [app web-port] :as web-config} (read-web-config config)
        {handler :frodo/handler, :as system} (start! app)]

    (println "Starting web server, port" web-port)

    (let [server (start-httpkit! handler (merge {:port web-port :join? false}
                                                (:http-kit/options web-config)))]
      (dosync
       (ref-set !instance {:server server
                           :app app
                           :system system})
       nil))))

(defn- stop-instance! [!instance]
  (when-let [{:keys [server app system]} (dosync
                                          (let [instance @!instance]
                                            (ref-set !instance nil)
                                            instance))]
    (println "Stopping web server.")
    (server)
    (stop! app system)
    nil))

(defn- reload-instance! [!instance config]
  (stop-instance! !instance)
  (refresh)
  (start-instance! !instance config))

(defn init-web! [_config]
  (let [!instance (ref nil)]
    (intern 'user 'start-frodo! #(do (refresh) (start-instance! !instance (_config))))
    (intern 'user 'stop-frodo! #(stop-instance! !instance))
    (intern 'user 'reload-frodo! #(reload-instance! !instance (_config)))

    (start-instance! !instance (_config))))
