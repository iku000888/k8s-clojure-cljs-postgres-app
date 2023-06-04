(ns health-record-app.sql
  (:require [next.jdbc.sql :as sql]
            [next.jdbc :as jdbc]
            [next.jdbc.types :refer [as-other]]
            [next.jdbc.date-time]))

(defn patients [conn]
  (sql/query conn ["select * from patients"] {:builder-fn next.jdbc.result-set/as-unqualified-maps}))

(defn add-patient [conn patient]
  (sql/insert! conn :patients patient {:builder-fn next.jdbc.result-set/as-unqualified-maps}))

(defn update-patient [conn id patient]
  (sql/update! conn :patients patient {:patients/patient_id id}))

(defn delete-patient [conn id]
  (sql/delete! conn :patients {:patients/patient_id id}))

(comment
  (with-open [c (jdbc/get-connection (:db juxt.clip.repl/system))]
    (add-patient c {:name "Ikuru"
                    :gender (as-other "Male")
                    :date_of_birth (java.time.LocalDate/parse "1992-08-23")
                    :address "Fresno, CA"
                    :phone_number "650 605 7491"}))
  (with-open [c (jdbc/get-connection (:db juxt.clip.repl/system))]
    (patients c))
  (with-open [c (jdbc/get-connection (:db juxt.clip.repl/system))]
    (update-patient c 1 {:name "foobar"}))
  (with-open [c (jdbc/get-connection (:db juxt.clip.repl/system))]
    (delete-patient c 1))


  )
