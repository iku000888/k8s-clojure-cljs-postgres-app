(ns health-record-app.server
  (:require [aleph.http :as http]
            [bidi.ring]
            [yada.yada :as yada]))

(defn aleph-server [{:keys [resources
                            handlers
                            port]}]
  (yada/listener
   ["/api/" {"patients/"
             {"" (yada.yada/resource (:patients resources))
              [[long :patient-id]] (yada.yada/resource (:patient resources))}}]
   {:port port :raw-stream? true}))

(defn aleph-server.stop [server]
  (.close (:server server)))
