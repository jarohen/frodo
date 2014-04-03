(ns lein-frodo.plugin
  (:require [clojure.java.io :as io]))

(defn with-frodo-core-dep [project]
  (update-in project [:dependencies]
             conj
             ['jarohen/frodo-core (slurp (io/resource "FRODO-VERSION"))]))

(defn middleware [project]
  (with-frodo-core-dep project))
