(ns sample-project.handler
  (:require [ring.util.response :refer [response]]
            [compojure.core :refer [defroutes GET]]
            [compojure.route :refer [resources]]
            [compojure.handler :refer [api]]
            [hiccup.page :refer [html5 include-css include-js]]
            [frodo :refer [repl-connect-js]]))

(defn page-frame []
  (html5
   [:head
    [:title "sample-project - CLJS Single Page Web Application"]
    (include-js "//cdnjs.cloudflare.com/ajax/libs/jquery/2.0.3/jquery.min.js")
    (include-js "//netdna.bootstrapcdn.com/bootstrap/3.0.0/js/bootstrap.min.js")
    (include-css "//netdna.bootstrapcdn.com/bootstrap/3.0.0/css/bootstrap.min.css")

    (include-js "/js/sample-project.js")]
   [:body
    [:div.container
     [:div#content]]
    [:script (repl-connect-js)]]))

(defroutes app-routes
  (GET "/" [] (response (page-frame)))
  (resources "/js" {:root "js"}))

(def app 
  (-> app-routes
      api))
