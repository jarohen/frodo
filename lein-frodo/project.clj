(defproject jarohen/lein-frodo (slurp (clojure.java.io/file "../common/FRODO-VERSION"))
  :description "A Lein plugin to start an HTTP-kit server via configuration in Nomad"
  :url "https://github.com/james-henderson/lein-frodo.git"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.5.1"]
                 [leinjacker "0.4.1"]
                 [jarohen/nomad "0.6.0"]]

  :resource-paths ["resources" "../common"]
  
  :eval-in-leiningen true)