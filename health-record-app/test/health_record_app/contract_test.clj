(ns health-record-app.contract-test
  (:require [clojure.test :refer :all]
            [aleph.http :as http]
            [manifold.deferred :as d]
            [clojure.java.io :as io])
  (:import [au.com.dius.pact.consumer ConsumerPactBuilder PactTestRun]
           [au.com.dius.pact.consumer.model MockProviderConfig]
           [au.com.dius.pact.core.model PactSpecVersion DefaultPactReader]
           [au.com.dius.pact.consumer ConsumerPactRunnerKt]
           [au.com.dius.pact.provider ProviderInfo ConsumerInfo ProviderVerifier]))

(.toMap
 (-> (ConsumerPactBuilder/consumer "Consumer")
     (.hasPactWith "Provider")
     (.uponReceiving "a request to say Hello")
     (.path "/hello")
     (.method "POST")
     (.body "{\"name\": \"harry\"}")
     (.willRespondWith)
     (.status 200)
     (.body "{\"hello\": \"harry\"}")
     (.toPact))
 PactSpecVersion/V4)
(println
 (spit "pactt.json"
       (cheshire.core/generate-string
        {"provider" {"name" "Provider"}, "consumer" {"name" "Consumer"}, "interactions" [{"description" "a request to say Hello", "request" {"method" "POST", "path" "/hello", "body" {"name" "harry"}}, "response" {"status" 200, "body" {"hello" "harry"}}}], "metadata" {"pactSpecification" {"version" "4.0"}, "pact-jvm" {"version" "4.6.0"}}})))


(deftest basic-pact-test
  (ConsumerPactRunnerKt/runConsumerTest
   (.toMap
    (-> (ConsumerPactBuilder/consumer "Consumer")
        (.hasPactWith "Provider")
        (.uponReceiving "a request to say Hello")
        (.path "/hello")
        (.method "POST")
        (.body "{\"name\": \"harry\"}")
        (.willRespondWith)
        (.status 200)
        (.body "{\"hello\": \"harry\"}")
        (.toPact))
    PactSpecVersion/V4)
   (MockProviderConfig/createDefault)
   (reify
     PactTestRun
     (run [this mock-server context]
       (let [{:keys [body status]}
             @(http/post (str (.getUrl mock-server) "/hello")
                         {:body (cheshire.core/encode {:name "harry"})})]
         (is (= {:hello "harry"} (cheshire.core/decode (slurp body) keyword)))
         (is (= 200 status)))))))

(let [p (doto (ProviderInfo. "p")
          (.setProtocol "http")
          (.setHost "localhost")
          (.setPort 8080)
          (.setPath "/"))
      c (doto (ConsumerInfo. "c")
          (.setName "consumer")
          (.setPactSource (io/file "pactt.json")))
      interactions (into []
                         (.getInteractions
                          (.loadPact DefaultPactReader/INSTANCE
                                     (.getPactSource c))))
      v (doto (ProviderVerifier.)
          (.initialiseReporters p)
          (.reportVerificationForConsumer c p (.getPactSource c)))]
  )

;; serviceProvider = new ProviderInfo('Dropwizard App')
;; serviceProvider.setProtocol('http')
;; serviceProvider.setHost('localhost')
;; serviceProvider.setPort(8080)
;; serviceProvider.setPath('/')
