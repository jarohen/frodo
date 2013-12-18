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
      (deps/add-if-missing '[http-kit "2.1.12"])
      (deps/add-if-missing '[org.clojure/tools.nrepl "0.2.3"])
      (deps/add-if-missing '[org.clojure/tools.namespace "0.2.4"])

      (cond-> (cljs-repl? config) (deps/add-if-missing '[com.cemerick/austin "0.1.3"]))))

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
       (println "Started nREPL server, port" ~nrepl-port))))

(defn handler-deprecation-warning! []
  (binding [*out* *err*]
    (println (str "WARN: Frodo's :handler now belongs "
                  "inside the :web map. "
                  "Please see https://github.com/james-henderson/lein-frodo "
                  "for more information. "
                  "The original behaviour will be "
                  "removed in v0.3.0."))))

(defn run-web-form [config]
  (when-let [web-port (get-in config [:web :port])]
    (let [handler-fn (or (get-in config [:web :handler-fn])
                         (when-let [handler (or (get-in config [:web :handler])
                                                (when-let [old-handler (:handler config)]
                                                  (handler-deprecation-warning!)
                                                  old-handler))]
                           `(fn [] (ns-resolve *ns* '~handler))))]
      (assert handler-fn "Please configure a handler in your Nomad configuration.")
      (doto `(do
               (binding [*ns* (create-ns '~'user)]
                 (refer '~'frodo.server :only '~'[start-frodo! stop-frodo! reload-frodo!]))

               (intern (create-ns '~'frodo.server) '~'handler-fn ~handler-fn)
               (intern (create-ns '~'frodo.server) '~'web-port ~web-port)

               (frodo.server/start-frodo!))
        prn))))

(defn make-form [project config]
  `(do
     ~(run-nrepl-form project config)
     ~(run-web-form config)))

(defn requires-form [project config]
  `(do
     (require 'clojure.tools.nrepl.server)
     (require 'frodo.server)
     ~@(when (cljs-repl? config)
         `[(require 'cemerick.piggieback)
           (require 'cemerick.austin)
           (require 'cemerick.austin.repls)])

     ~@(when (:cljx project)
         `[(require 'cljx.repl-middleware)])
     
     (require '~(symbol (namespace (or (get-in config [:web :handler-fn])
                                       (get-in config [:web :handler])
                                       (:handler config)))))))

(defn copy-to-classpath [project file path]
  (let [classpath-file-path (doto (-> (io/file (:target-path project) "classes" path)
                                      (.getAbsolutePath))
                              (io/make-parents))]
    (clojure.java.io/copy (slurp file)
                          (clojure.java.io/file classpath-file-path))))

(defn copy-frodo-nses! [project]
  (copy-to-classpath project (io/resource "frodo.clj") "frodo.clj")
  (copy-to-classpath project (io/resource "frodo/server.clj") "frodo/server.clj"))

(defn frodo
  [project & args]
  (let [nomad-file (get-nomad-file project)
        nomad-config (load-nomad-config nomad-file)]
    (copy-frodo-nses! project)
    (eval-in-project (add-ring-deps project nomad-config)
                     (make-form project nomad-config)
                     (requires-form project nomad-config))))
