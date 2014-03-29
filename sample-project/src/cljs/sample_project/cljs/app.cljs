(ns sample-project.cljs.app
  (:require [dommy.core :as d]
            [clojure.string :as s]
            frodo.brepl)
    (:require-macros [dommy.macros :refer [node sel1]]))

(defn code [s]
  (node [:span {:style {:font-family "Courier New"}} s]))

(set! (.-onload js/window)
      (fn []
        (d/replace-contents! (sel1 :#content)
                             (node [:div
                                    [:h2 {:style {:margin-top :1em}}
                                     "Hello world from Frodo!"]

                                    [:h3 "Things to try:"]
                                    
                                    [:ul
                                     [:li [:p "In your Clojure REPL (in the 'user' ns), run " (code "(reload-frodo!)") " to completely reload the webapp. The 'started at' time above should change."]]
                                     [:li [:p "Connect to a CLJS bREPL by running " (code "(frodo-brepl)")]]
                                     [:li
                                      [:p "Once you've opened the bREPL, reload your browser to make the connection, then you can eval some CLJS."]
                                      [:p "I recommend:"]
                                      
                                      [:ul
                                       [:li (code "(js/alert \"Hello world!\")")]
                                       [:li (code "(set! (.-backgroundColor js/document.body.style) \"green\")")]
                                       [:li (code (s/join "\n" ["(in-ns 'sample-project.cljs.app)"
                                                                "(d/replace-contents! (sel1 :#sample-div) (node [:strong \"Hi!\"]))"]))]]

                                      [:p "Run " (code ":cljs/quit") " to get back to a Clojure REPL."]]]
                                    [:div#sample-div]]))))


