(ns sample-project.cljs.home
  (:require [dommy.core :as d])
  (:require-macros [dommy.macros :refer [node sel1]]))

(defn watch-hash! [current-hash]
  (add-watch current-hash :blog-page
             (fn [_ _ _ hash]
               (d/replace-contents! (sel1 :#content) (node [:h2 {:style {:margin-top :1em}}
                                                            "Hello world from ClojureScript!"])))))
