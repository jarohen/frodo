(defproject sample-project ""

  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  
  :dependencies [[org.clojure/clojure "1.5.1"]

                 [ring/ring-core "1.2.0"]
                 [compojure "1.1.5"]
                 [hiccup "1.0.4"]

                 [prismatic/dommy "0.1.1"]

                 [org.clojure/clojurescript "0.0-2156"]
                 [org.clojure/tools.reader "0.8.3"]
                 [jarohen/frodo-core "0.2.12-SNAPSHOT"]
                 [weasel "0.1.0"]]

  :plugins [[jarohen/lein-frodo "0.2.12-SNAPSHOT"]
            [lein-cljsbuild "1.0.0-alpha2"]
            [lein-pdo "0.1.1"]]

  :frodo/config-resource "sample-project-config.edn"

  :source-paths ["src/clojure"]

  :resource-paths ["resources" "target/resources"]

  :cljsbuild {:builds [{:source-paths ["src/cljs"]
                        :compiler {:output-to "target/resources/js/sample-project.js"
                                   :optimizations :whitespace
                                   :pretty-print true}}]}

  :aliases {"dev" ["pdo" "cljsbuild" "auto," "frodo"]
            "start" ["do" "cljsbuild" "once," "trampoline" "frodo"]})


