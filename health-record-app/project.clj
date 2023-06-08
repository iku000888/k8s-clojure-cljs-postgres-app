(defproject health-record-app "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[com.amperity/dialog "2.0.115"]
                 [org.clojure/clojure "1.10.3"]
                 [yada "1.2.15" :exclusions [[byte-streams]]]

                 ;; https://github.com/juxt/yada/issues/266
                 #_[aleph "0.4.7-alpha5"]
                 [aleph "0.6.2"]
                 [org.clj-commons/byte-streams "0.3.2"]

                 [manifold "0.4.1"]
                 [juxt/clip "0.28.0"]
                 [aero "1.1.6"]



                 [com.github.seancorfield/next.jdbc "1.3.874"]
                 [com.zaxxer/HikariCP "3.3.1"]
                 [org.postgresql/postgresql "42.2.10"]]

  :profiles {:dev {:dependencies [[org.clojure/tools.namespace "1.4.4"]
                                  [nubank/mockfn "0.7.0"]]
                   :plugins [[lein-cloverage "1.2.2"]]
                   :source-paths ["dev/src"]
                   :repl-options {:init-ns dev}}
             :kaocha {:dependencies [[lambdaisland/kaocha "1.84.1335"]
                                     [lambdaisland/kaocha-cucumber "0.11.100"]]
                      :source-paths ["bdd"]}

             :uberjar {:main health-record-app.core
                       :aot [health-record-app.core]}}
  :aliases {"kaocha" ["with-profile" "+kaocha" "run" "-m" "kaocha.runner"]})
