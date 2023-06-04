(ns health-record-app.server
  (:require [aleph.http :as http]
            [bidi.ring]
            [yada.yada :as yada]
            [dialog.logger :as log]))

(defn log-request [ctx]
  (log/info (:uri (:request ctx)) (:request-method (:request ctx)))
  ctx)

(defn log-response [ctx]
  (log/info (:status (:response ctx)) (:uri (:request ctx)) (:request-method (:request ctx)))
  ctx)

(defn dope-resource [res]
  (assoc
   res
   :responses {500 {:produces "application/json"
                    :response {:message "Sorry, very internal error"}}}
   :access-control {:allow-origin #{(System/getenv "CORS_ALLOW_ORIGIN")
                                    "http://localhost:8700"}
                    :allow-methods #{:get :put :post :delete}
                    :allow-headers #{"content-type"}}
   :interceptor-chain [log-request
                       yada.interceptors/method-allowed?
                       yada.swagger-parameters/parse-parameters
                       yada.interceptors/capture-proxy-headers
                       yada.interceptors/get-properties
                       yada.interceptors/process-content-encoding
                       yada.interceptors/process-request-body
                       yada.interceptors/select-representation
                       yada.interceptors/if-match
                       yada.interceptors/if-none-match
                       yada.interceptors/invoke-method
                       yada.interceptors/get-new-properties
                       yada.security/access-control-headers
                       yada.security/security-headers
                       yada.interceptors/create-response
                       log-response
                       yada.interceptors/return]
   :error-interceptor-chain [yada.security/access-control-headers
                             (fn [ctx]
                               (log/error (:error ctx)
                                          ctx
                                          "Uncaught error")
                               (update ctx :response assoc :body {:message "Internal Error"}))

                             yada.interceptors/create-response
                             yada.interceptors/return]))

(defn aleph-server [{:keys [resources
                            port]}]
  (let [resources+ (into {} (map (fn [[k v]]
                                   [k (dope-resource v)])
                                 resources))]
    (yada/listener
     ["/api/" {"patients/"
               {"" (yada.yada/resource (:patients resources+))
                [[long :patient-id]] (yada.yada/resource (:patient resources+))}}]
     {:port port :raw-stream? true})))

(defn aleph-server.stop [server]
  (.close (:server server)))
