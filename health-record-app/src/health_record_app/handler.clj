(ns health-record-app.handler
  (:require [health-record-app.sql :as sql]
            [next.jdbc :as jdbc]))

(defn ->sql-patient [{:keys [gender date_of_birth] :as patient}]
  (cond-> patient
    gender (update :gender next.jdbc.types/as-other)
    date_of_birth (update :date_of_birth #(java.time.LocalDate/parse %))))

(defn patients.add [db]
  (fn add-patient [req]
    (with-open [c (jdbc/get-connection db)]
      (sql/add-patient c (->sql-patient (:body req))))))

(defn patients.update [db]
  (fn update-patient [req]
    (with-open [c (jdbc/get-connection db)]
      (sql/update-patient c
                          (:patient-id (:route-params req))
                          (->sql-patient (:body req))))))

(defn patient.delete [db]
  (fn delete-patient [req]
    (with-open [c (jdbc/get-connection db)]
      (sql/delete-patient c (:patient-id (:route-params req))))))

(defn patients [db]
  (fn patients [req]
    (with-open [c (jdbc/get-connection db)]
      (sql/patients c))))