(ns leiningen.frodo
  (:require [leinjacker.deps :as deps]
            [leinjacker.eval :refer [eval-in-project]]
            [clojure.java.io :as io]
            [leinjacker.utils :refer [get-classpath]]

            [nomad :refer [defconfig]])
  (:import [java.net URL URLClassLoader]))


(defn get-nomad-file [{:keys [source-paths resource-paths root] :as project}]
  (let [paths (->> (concat source-paths resource-paths)
                   (map io/file)
                   (map io/as-url))
        cl (URLClassLoader. (into-array paths))]
    
    (.getResource cl (get project :frodo/config-resource))))

(defn load-nomad-config [nomad-file]
  ;; A bit of a hack, but unfortunately Nomad doesn't currently have a
  ;; 'load config file once' fn...
  (defconfig _config-var nomad-file)
  (get (_config-var) :frodo/config))

(defn cljs-repl? [config]
  (boolean (get-in config [:nrepl :cljs-repl?])))

(defn add-ring-deps [project config]
  (-> project
      (deps/add-if-missing '[http-kit "2.1.10"])
      (deps/add-if-missing '[org.clojure/tools.nrepl "0.2.3"])
      
      (cond->
       (cljs-repl? config)
       (deps/add-if-missing '[com.cemerick/austin "0.1.1"]))))

(defn austin-handler-form [config]
  (when (cljs-repl? config)
    `(clojure.tools.nrepl.server/default-handler
       #'cemerick.piggieback/wrap-cljs-repl)))

(defn run-nrepl-form [config]
  (when-let [nrepl-port (get-in config [:nrepl :port])]
    `(do
       (doto (io/file "target/repl-port")
         (spit ~nrepl-port)
         (.deleteOnExit))
       (clojure.tools.nrepl.server/start-server :port ~nrepl-port
                                                :handler ~(austin-handler-form config))
       (println "Started nREPL server, port" ~nrepl-port))))

(defn run-web-form [config]
  (when-let [web-port (get-in config [:web :port])]
    (let [handler (get config :handler)]
      (assert handler "Please configure a handler in your Nomad configuration.")
      `(do
         (org.httpkit.server/run-server (var ~handler) {:port ~web-port :join? false})
         (println "Started web server, port" ~web-port)))))

(defn cljs-repl-fn [config]
  (when (cljs-repl? config)
    `(do
       (create-ns '~'frodo)
       (intern '~'frodo '~'cljs-repl
               (fn []
                 (cemerick.austin.repls/cljs-repl
                  (reset! cemerick.austin.repls/browser-repl-env
                          (cemerick.austin/repl-env))))))))

(defn make-form [config]
  `(do
     ~(run-nrepl-form config)
     ~(cljs-repl-fn config)
     ~(run-web-form config)))

(defn requires-form [{:keys [handler] :as config}]
  `(do
     (require 'clojure.tools.nrepl.server)
     (require 'org.httpkit.server)
     (require '~(symbol (namespace handler)))
     ~@(when (cljs-repl? config)
         `[(require 'cemerick.piggieback)
           (require 'cemerick.austin)
           (require 'cemerick.austin.repls)])))

(defn frodo
  [project & args]
  (let [nomad-file (get-nomad-file project)
        nomad-config (load-nomad-config nomad-file)]
    (eval-in-project (add-ring-deps project nomad-config)
                     (make-form nomad-config)
                     (requires-form nomad-config))))
