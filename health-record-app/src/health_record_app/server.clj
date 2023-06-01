(ns health-record-app.server
  (:require [aleph.http :as http]
            [yada.yada :as yada]))

(defn aleph-server [{:keys [handler port]}]
  (http/start-server
   handler
   {:port port}))

(defn aleph-server.stop [server]
  (doto (.close server)
    prn))

(defn yada-handler [config]
  (yada/handler
   {:methods
    {:get
     {:produces "text/html"
      :response "<h1>Hello World!</h1>"}}}))
