(ns health-record-app.handler
  (:require [health-record-app.sql :as sql]
            [health-record-app.schema :as schema]
            [next.jdbc :as jdbc]
            [malli.core :as m]
            [malli.error :as me]))

;; The as-other values are not interned and equality breaks
;; across every call making testing harder.
;; Making it static allows re-use of the same instance and maintains equality
(def ->sqlenum
  {"Male" (next.jdbc.types/as-other "Male")
   "Female" (next.jdbc.types/as-other "Female")})

(defn ->sql-patient [{:keys [gender date_of_birth] :as patient}]
  (cond-> patient
    gender (update :gender ->sqlenum)
    date_of_birth (update :date_of_birth #(java.time.LocalDate/parse %))))

(defn patients.add [db]
  (fn add-patient [req]
    (if-let [explanation (m/explain schema/PatientSchema
                                    (:body req))]
      (throw (ex-info "Validation failure"
                      {:http/status 400
                       :body (me/humanize explanation)}))
      (with-open [c (jdbc/get-connection db)]
        (sql/add-patient c (->sql-patient (:body req)))))))

(defn patients.update [db]
  (fn update-patient [req]
    (if-let [explanation (m/explain schema/PatientSchema
                                    (:body req))]
      (throw (ex-info "Validation failure"
                      {:http/status 400
                       :body (me/humanize explanation)}))
      (with-open [c (jdbc/get-connection db)]
        (sql/update-patient c
                            (:patient-id (:route-params req))
                            (->sql-patient (:body req)))))))

(defn patient.delete [db]
  (fn delete-patient [req]
    (with-open [c (jdbc/get-connection db)]
      (sql/delete-patient c (:patient-id (:route-params req))))))

(defn patients [db]
  (fn patients [req]
    (with-open [c (jdbc/get-connection db)]
      (sql/patients c))))

(defn- pact-setup-enabled? []
  (= "true"
     (System/getenv "_DANGEROUSLY_ENABLE_PACT_SETUP")))

(defn pact-setup [db]
  (when (pact-setup-enabled?)
    (fn delete-all-patients [req]
      (prn (:body req))
      (prn (:headers (:request req)))
      (when (and (= "No patients exist"
                    (get-in req [:body :state])))
        (with-open [c (jdbc/get-connection db)]
          (doseq [{:keys [patient_id]} (sql/patients c)]
            (sql/delete-patient c patient_id)))))))
