(ns frodo.foo)

(defn foo-handler-fn []
  (let [started-at (java.util.Date.)]
    (fn [req]
      {:status 200
       :body (str "Server started at " started-at)})))
