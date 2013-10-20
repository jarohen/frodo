(ns frodo)

(defmacro when-cljs-repl-enabled [& body]
  `(when (find-ns '~'cemerick.austin.repls)
     (eval (do '~@body))))

(when-cljs-repl-enabled
 (defn cljs-repl []
   (cemerick.austin.repls/cljs-repl
    (reset! cemerick.austin.repls/browser-repl-env
            (cemerick.austin/repl-env)))))

(defn repl-connect-js []
  (when-cljs-repl-enabled
   (cemerick.austin.repls/browser-connected-repl-js)))
