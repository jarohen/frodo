(ns sample-project.cljs.app
    (:require [clojure.string :as s]
              [sample-project.cljs.home :as home]
              clojure.browser.repl)
    (:require-macros [dommy.macros :refer [sel sel1]]))

(def default-hash "#/")

(defn- bind-hash [hash-atom]
  (letfn [(on-hash-change []
            (reset! hash-atom (.-hash js/location)))]
    
    (set! (.-onhashchange js/window) on-hash-change)
    
    (when (s/blank? (.-hash js/location))
      (set! (.-hash js/location) default-hash))
    
    (on-hash-change)))

(set! (.-onload js/window)
      (fn []
        (let [hash-atom (atom nil)]
          (home/watch-hash! hash-atom)
          (bind-hash hash-atom))))


