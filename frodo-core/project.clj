(defproject jarohen/frodo-core (slurp (clojure.java.io/file "../common/FRODO-VERSION"))
  :description "A Lein plugin to start an HTTP-kit server via configuration in Nomad"
  :url "https://github.com/james-henderson/frodo.git"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [http-kit "2.1.18"]
                 [org.clojure/tools.nrepl "0.2.3"]
                 [org.clojure/tools.namespace "0.2.4"]
                 [alembic "0.2.0"]
                 
                 [jarohen/nomad "0.6.3"]]

  :scm {:dir ".."}

  :aot [frodo.main])
