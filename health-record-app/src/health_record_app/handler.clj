(ns health-record-app.handler
  (:require [health-record-app.sql :as sql]
            [next.jdbc :as jdbc]))

(defn patients.add [db]
  (fn add-patient [req]
    (with-open [c (jdbc/get-connection db)]
      #_(prn req)
      {:boo "bor"}
      #_(sql/add-patient c (:patient-id (:route-params req))))))

(defn patient.delete [db]
  (fn delete-patient [req]
    (with-open [c (jdbc/get-connection db)]
      (sql/delete-patient c (:patient-id (:route-params req))))))

(defn patients [db]
  (fn patients [req]
    (with-open [c (jdbc/get-connection db)]
      (sql/patients c))))
