(defproject health-record-app "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.3"]
                 [yada "1.2.15"]

                 ;; https://github.com/juxt/yada/issues/266
                 [aleph "0.6.2"]

                 [juxt/clip "0.28.0"]
                 [aero "1.1.6"]]
  :profiles {:dev {:dependencies [[org.clojure/tools.namespace "1.4.4"]]
                   :source-paths ["dev/src"]
                   :repl-options {:init-ns dev}}
             :uberjar {:main health-record-app.core
                       :aot [health-record-app.core]}})
