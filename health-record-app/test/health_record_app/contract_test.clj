(ns health-record-app.contract-test
  (:require [clojure.test :refer :all]
            [aleph.http :as http]
            [manifold.deferred :as d])
  (:import [au.com.dius.pact.consumer ConsumerPactBuilder PactTestRun]
           [au.com.dius.pact.consumer.model MockProviderConfig]
           [au.com.dius.pact.consumer ConsumerPactRunnerKt]))

(deftest basic-pact-test
  (ConsumerPactRunnerKt/runConsumerTest
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
   (MockProviderConfig/createDefault)
   (reify
     PactTestRun
     (run [this mock-server context]
       (let [{:keys [body status]}
             @(http/post (str (.getUrl mock-server) "/hello")
                         {:body (cheshire.core/encode {:name "harry"})})]
         (is (= {:hello "harry"} (cheshire.core/decode (slurp body) keyword)))
         (is (= 200 status)))))))
