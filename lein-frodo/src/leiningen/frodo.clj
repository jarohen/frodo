(ns leiningen.frodo
  (:require [leinjacker.deps :as deps]
            [leinjacker.eval :refer [eval-in-project]]
            [leiningen.uberjar :as u]
            [clojure.java.io :as io]
            [leinjacker.utils :refer [get-classpath]]

            [nomad :refer [defconfig]]))

(defn add-core-dep [project]
  (-> project
      (deps/add-if-missing '[jarohen/frodo-core "0.2.6-SNAPSHOT"])))

(defn server
  "Starts the Frodo application, as per the configuration file specified in project.clj.

   Usage: lein frodo [server]"
  [project]
  
  (let [nomad-resource (:frodo/config-resource project)]
    (eval-in-project (add-core-dep project)
                     `(#'frodo.core/init-frodo! (clojure.java.io/resource ~nomad-resource)
                                                {:cljx? ~(boolean (:cljx project))
                                                 :target-path ~(:target-path project)})
                     `(require '~'frodo.core))))

(defn uberjar-project-map [project]
  (let [nomad-resource (:frodo/config-resource project)]
    (-> project
        add-core-dep
        (assoc :main 'frodo.main)
        (update-in [:aot] conj 'frodo.main)
        (update-in [:filespecs] conj {:type :bytes
                                      :path "META-INF/frodo-config-resource"
                                      :bytes nomad-resource}))))

(defn uberjar
  "Creates an uberjar of the Frodo application

   Usage: lein frodo uberjar"
  [project]
  
  (let [project (-> project
                    uberjar-project-map
                    (vary-meta #(update-in % [:without-profiles] uberjar-project-map)))]
    (u/uberjar project 'frodo.main)))

(defn frodo
  "Plugin to start an nREPL/web server.

   Usage: lein frodo [server, uberjar]

   If no arguments are provided, 'server' is assumed.

   For more details of how to set up and use Frodo, please refer to
   the documentation at https://github.com/james-henderson/frodo"
  {:help-arglists '([] [server] [uberjar])
   :subtasks [#'server #'server #'uberjar]}
  
  [project & [command & args]]
  
  (case command
    "uberjar" (uberjar project)
    "server" (server project)
    nil (server project)))
