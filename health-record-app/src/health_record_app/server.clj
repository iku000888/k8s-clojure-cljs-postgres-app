(ns health-record-app.server
  (:require [aleph.http :as http]
            [bidi.ring]
            [yada.yada :as yada]
            [dialog.logger :as log]))
(yada.handler/interceptor-chain nil)
(comment
  (let [body (:body @((yada.handler/as-handler
                       ["/"
                        (yada.yada/resource
                         {:interceptor-chain [(fn [_] (throw (ex-info "i hate u" {})))]}
                         )])
                      {:uri "/"}))
        buf (byte-array (.remaining body))]
    (.get body buf)
    (slurp buf))

  (let [body (:body @((yada.handler/as-handler
                       ["/"
                        (yada.yada/resource
                         {:interceptor-chain
                          [#_(fn [_] (throw (ex-info "i hate u" {})))]
                          #_(yada.handler/interceptor-chain nil)
                          #_(yada.handler/error-interceptor-chain nil)
                          :error-interceptor-chain
                          [#_(fn [ctx]
                               #_(throw (ex-info "i hate u more" {}))
                               {:body
                                (yada.body/to-body "goober" String)})]
                          :methods
                          {:post
                           {:produces "application/json"
                            :consumes #{"application/json"}
                            :response (juxt.clip.repl/system :handler/patients.add)}
                           :get
                           {:produces "application/json"
                            :response (juxt.clip.repl/system :handler/patients)}}}

                         )])
                      {:uri "/"
                       :request-method :get}))
        buf (byte-array (.remaining body))]
    (.get body buf)
    (slurp buf))



  (:server juxt.clip.repl/system)
  )
(defn log-request [ctx]
  (log/info (:uri (:request ctx)) (:request-method (:request ctx)))
  ctx)

(defn log-response [ctx]
  (log/info (:status (:response ctx)) (:uri (:request ctx)) (:request-method (:request ctx)))
  ctx)


(defn dope-resource [res]
  (assoc
   res
   :interceptor-chain
   [log-request
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
   :error-interceptor-chain

   [(fn [ctx]
      (log/error (:error ctx)
                 "Uncaugh error")
      {:status 500
       :body
       (yada.body/to-body
        "Internal Error"
        String)})]))

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
