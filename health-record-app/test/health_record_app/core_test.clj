(ns health-record-app.core-test
  (:require [clojure.test :refer :all]
            [cheshire.core :as che]
            [health-record-app.core :refer :all]
            [mockfn.macros :as mfn]
            [mockfn.matchers :as matchers]
            [juxt.clip.core :as clip :refer [with-system]]
            [health-record-app.core :as core]
            [health-record-app.db :as db]
            [health-record-app.server :as server]
            [yada.handler :refer [as-handler]]
            [next.jdbc :as jdbc]
            [next.jdbc.sql :as sql]
            [health-record-app.handler :as handler]))

(def conn-mock
  (reify java.sql.Connection
    (close [this] nil)))

(deftest pact-setup-test
  (mfn/providing
   [(#'handler/pact-setup-enabled?) true

    (db/pool (matchers/any)) ::pool
    (db/pool.stop ::pool) nil
    (jdbc/get-connection ::pool) conn-mock

    (sql/query conn-mock ["select * from patients"]
               {:builder-fn next.jdbc.result-set/as-unqualified-maps})
    [{:patient_id 1}]

    (sql/delete! conn-mock :patients {:patients/patient_id 1}) nil]
   ((handler/pact-setup ::pool)

    {:body {:state "No patients exist"}}))
  )

(defn yada-body->json [b]
  (let [ba (byte-array (.remaining b))]
    (.get b ba)
    (che/decode
     (slurp ba)
     keyword)))

(deftest handler-tests
  (mfn/providing
   [(db/pool (matchers/any)) ::pool
    (db/pool.stop ::pool) nil
    (jdbc/get-connection ::pool) conn-mock]
   (with-system [s (update (core/config) :components dissoc :server)]
     (let [handler (as-handler (server/routes (:resources s)))]
       (testing "GET /api/patients/ - db mocked"
         (is (= [{:name "Joel",
                  :gender "Male",
                  :address "200 ln",
                  :phone_number "333 444 8888",
                  :date_of_birth "2000-10-11"}]
                (-> (mfn/providing
                     [(sql/query conn-mock ["select * from patients"]
                                 {:builder-fn next.jdbc.result-set/as-unqualified-maps})
                      [{:name "Joel" :gender "Male" :address "200 ln"
                        :phone_number "333 444 8888" :date_of_birth "2000-10-11"}]]
                     @(handler {:request-method :get
                                :uri "/api/patients/"}))
                    :body
                    yada-body->json))))
       (testing "POST /api/patients/ - db mocked"
         (let [patient {:name "Joel",
                        :gender "Male",
                        :address "200 ln",
                        :phone_number "333 444 8888",
                        :date_of_birth "2000-10-11"}]
           (is (= patient
                  (-> (mfn/providing
                       [(sql/insert! conn-mock :patients (handler/->sql-patient patient)
                                     {:builder-fn next.jdbc.result-set/as-unqualified-maps})
                        patient]
                       @(handler {:headers {"content-type" "application/json"
                                            "content-length" "65"}
                                  :request-method :post
                                  :uri "/api/patients/"
                                  :body (che/encode
                                         {:name "Joel",
                                          :gender "Male",
                                          :address "200 ln",
                                          :phone_number "333 444 8888",
                                          :date_of_birth "2000-10-11"})}))
                      :body
                      yada-body->json)))))
       (testing "PUT /api/patients/1 - db mocked"
         (let [patient {:name "Joel",
                        :gender "Male",
                        :address "200 ln",
                        :phone_number "333 444 8888",
                        :date_of_birth "2000-10-11"}]
           (is (= {:next.jdbc/update-count 1}
                  (-> (mfn/providing
                       [(sql/update! conn-mock :patients
                                     (health-record-app.handler/->sql-patient patient)
                                     {:patients/patient_id 1})
                        {:next.jdbc/update-count 1}]
                       @(handler {:headers {"content-type" "application/json"
                                            "content-length" "65"}
                                  :request-method :put
                                  :uri "/api/patients/1"
                                  :body (che/encode
                                         {:name "Joel",
                                          :gender "Male",
                                          :address "200 ln",
                                          :phone_number "333 444 8888",
                                          :date_of_birth "2000-10-11"})}))
                      :body
                      yada-body->json)))))
       (testing "DELETE /api/patients/1 - db mocked"
         (is (= {:next.jdbc/update-count 1}
                (-> (mfn/providing
                     [(sql/delete! conn-mock :patients {:patients/patient_id 1})
                      {:next.jdbc/update-count 1}]
                     @(handler {:request-method :delete
                                :uri "/api/patients/1"}))
                    :body
                    yada-body->json))))))))

(deftest validation-tests
  (mfn/providing
   [(db/pool (matchers/any)) ::pool
    (db/pool.stop ::pool) nil
    (jdbc/get-connection ::pool) conn-mock]
   (with-system [s (update (core/config) :components dissoc :server)]
     (let [handler (as-handler (server/routes (:resources s)))
           bad-patient-data {:name nil
                             :gender "MMale",
                             :address nil
                             :phone_number "ggg 444 8888",
                             :date_of_birth "200B-10-11"}]
       (testing "POST /api/patients/ - db mocked"
         (is (= [{:name ["should be a string"],
                  :date_of_birth ["Date format is yyyy-MM-dd"],
                  :gender ["should be either Male or Female"],
                  :address ["should be a string"],
                  :phone_number ["Invalid phone number format"]}
                 400]
                (let [{:keys [body status]} @(handler {:headers {"content-type" "application/json"
                                                                 "content-length" "65"}
                                                       :request-method :post
                                                       :uri "/api/patients/"
                                                       :body (che/encode bad-patient-data)})]
                  [(yada-body->json body)
                   status]))))
       (testing "PUT /api/patients/1 - db mocked"
         (is (= [{:name ["should be a string"],
                  :date_of_birth ["Date format is yyyy-MM-dd"],
                  :gender ["should be either Male or Female"],
                  :address ["should be a string"],
                  :phone_number ["Invalid phone number format"]}
                 400]
                (let [{:keys [body status]} (-> @(handler {:headers {"content-type" "application/json"
                                                                     "content-length" "65"}
                                                           :request-method :put
                                                           :uri "/api/patients/1"
                                                           :body (che/encode bad-patient-data)}))]
                  [(yada-body->json body)
                   status]))))))))
