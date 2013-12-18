(ns leiningen.frodo
  (:require [leinjacker.deps :as deps]
            [leinjacker.eval :refer [eval-in-project]]
            [clojure.java.io :as io]
            [leinjacker.utils :refer [get-classpath]]

            [nomad :refer [defconfig]])
  (:import [java.net URL URLClassLoader]))

(defn add-core-dep [project]
  (-> project
      (deps/add-if-missing '[jarohen/frodo-core "0.2.6-SNAPSHOT"])))

(defn frodo
  [project & args]
  (let [nomad-resource (:frodo/config-resource project)]
    (eval-in-project (add-core-dep project)
                     `(#'frodo.core/init-frodo! (clojure.java.io/resource ~nomad-resource))
                     `(require '~'frodo.core))))
