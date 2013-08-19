(ns leiningen.nomad-ring
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
    
    (.getResource cl (get project :nomad-ring/resource))))

(defn load-nomad-config [nomad-file]
  ;; A bit of a hack, but unfortunately Nomad doesn't currently have a
  ;; 'load config file once' fn...
  (defconfig _config-var nomad-file)
  (get (_config-var) :nomad-ring))

(defn add-ring-deps [project]
  (-> project
      (deps/add-if-missing '[ring/ring-jetty-adapter "1.2.0"])
      (deps/add-if-missing '[org.clojure/tools.nrepl "0.2.3"])))

(defn run-nrepl-form [config]
  (when-let [nrepl-port (get-in config [:nrepl :port])]
    `(do
       (clojure.tools.nrepl.server/start-server :port ~nrepl-port)
       (println "Started nREPL server, port" ~nrepl-port))))

(defn run-web-form [config]
  (when-let [web-port (get-in config [:web :port])]
    (let [handler (get config :handler)]
      `(do
         (ring.adapter.jetty/run-jetty (var ~handler) {:port ~web-port :join? false})
         (println "Started web server, port" ~web-port)))))

(defn make-form [config]
  `(do
     ~(run-nrepl-form config)
     ~(run-web-form config)))

(defn requires-form [{:keys [handler] :as config}]
  `(do
     (require 'clojure.tools.nrepl.server)
     (require 'ring.adapter.jetty)
     (require (symbol ~(namespace handler)))))

(defn nomad-ring
  [project & args]
  (let [nomad-file (get-nomad-file project)
        nomad-config (load-nomad-config nomad-file)]
    
    (eval-in-project (add-ring-deps project)
                     (make-form nomad-config)
                     (requires-form nomad-config))))
