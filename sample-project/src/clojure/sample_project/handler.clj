(ns sample-project.handler
  (:require [ring.util.response :refer [response]]
            [compojure.core :refer [defroutes GET routes]]
            [compojure.route :refer [resources]]
            [compojure.handler :refer [api]]
            [hiccup.page :refer [html5 include-css include-js]]
            [frodo.brepl :refer [brepl-js]]
            [frodo.web :refer [App]]))

(defn page-frame [started-time]
  (html5
   [:head
    [:title "Frodo Sample Project"]
    [:script (brepl-js)]
    
    (include-js "//cdnjs.cloudflare.com/ajax/libs/jquery/2.0.3/jquery.min.js")
    (include-js "//netdna.bootstrapcdn.com/bootstrap/3.0.0/js/bootstrap.min.js")
    (include-css "//netdna.bootstrapcdn.com/bootstrap/3.0.0/css/bootstrap.min.css")

    (include-js "/js/sample-project.js")]
   
   [:body
    [:div.container
     [:span "Started at " (str started-time)]
     [:div#content]]]))

(defn app-routes [started-time]
  (routes
    (GET "/" [] (response (page-frame started-time)))
    (resources "/js" {:root "js"})))

(def app
  (reify App
    (start! [_]
      (let [started-time (java.util.Date.)]
        (println "Starting system")
        {:frodo/handler (-> (app-routes started-time)
                            api)
         ::started-time started-time}))
    (stop! [_ system]
      (println "Stopping system" (pr-str system)))))
