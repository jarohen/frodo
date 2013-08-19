(ns sample-project.web
  (:require [ring.util.response :refer [response]]))

(defn handler [req]
  (response "Hello world!"))
