(ns sample-project.handler
  (:require [ring.util.response :refer [response]]
            [compojure.core :refer [defroutes GET routes]]
            [compojure.route :refer [resources]]
            [compojure.handler :refer [api]]
            [hiccup.page :refer [html5 include-css include-js]]
            [frodo :refer [repl-connect-js]]))

(defn page-frame [started-time]
  (html5
   [:head
    [:title "sample-project - CLJS Single Page Web Application"]
    (include-js "//cdnjs.cloudflare.com/ajax/libs/jquery/2.0.3/jquery.min.js")
    (include-js "//netdna.bootstrapcdn.com/bootstrap/3.0.0/js/bootstrap.min.js")
    (include-css "//netdna.bootstrapcdn.com/bootstrap/3.0.0/css/bootstrap.min.css")

    (include-js "/js/sample-project.js")]
   [:body
    [:div.container
     [:span "Started at " (str started-time)]
     [:div#content]]
    [:script (repl-connect-js)]]))

#_(page-frame)

(defn app-routes [started-time]
  (routes
    (GET "/" [] (response (page-frame started-time)))
    (resources "/js" {:root "js"})))

(defn app [] 
  (-> (app-routes (java.util.Date.))
      api))
