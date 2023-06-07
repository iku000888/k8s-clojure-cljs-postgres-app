(defproject helix-demo "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.11.0"]
                 [thheller/shadow-cljs "2.23.3"]
                 [lilactown/helix "0.1.10"]
                 [cljs-ajax "0.8.4"]
                 [org.clojure/core.async "1.6.673"]]
  :plugins [[lein-shadow "0.4.1"]]
  :repl-options {:init-ns helix-demo.core}
  :npm-deps [[react "18.2.0"]
             [react-dom "18.2.0"]
             [smarthr-ui "31.1.0"]
             [styled-components "5.3.11"]
             [process "0.11.10"]
             ["@testing-library/react" "14.0.0"]
             ["@testing-library/user-event" "14.4.3"]]
  :shadow-cljs
  {:source-paths ["src" "test"]
   :nrepl        {:port 8777}
   :builds
   {:app {:target :browser
          :output-dir "public/js/compiled"
          :asset-path "/js/compiled"

          :modules
          {:main
           {:entries [health-record-frontend.core]}}

          :devtools
          ;; before live-reloading any code call this function
          {:before-load health-record-frontend.core/stop
           ;; after live-reloading finishes call this function
           :after-load health-record-frontend.core/start
           ;; serve the public directory over http at port 8700
           :http-root    "public"
           :http-port    8700}}

    :component-test
    {:target :browser-test
     :test-dir "tests"

     :devtools
     {:http-root    "tests"
      :http-port    8701}}
    }})
