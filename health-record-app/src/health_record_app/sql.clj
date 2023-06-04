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
  (doseq [i (range 100)]
    (with-open [c (jdbc/get-connection (:db juxt.clip.repl/system))]
      (add-patient c {:name (str "person-" i)
                      :gender (as-other (rand-nth ["Male" "Female"]))
                      :date_of_birth (java.time.LocalDate/parse
                                      (format "19%s%s-%s-%s"
                                              (rand-int 10)
                                              (rand-int 10)
                                              (rand-nth ["01"
                                                         "02"
                                                         "03"
                                                         "04"
                                                         "05"
                                                         "06"
                                                         "07"
                                                         "08"
                                                         "09"
                                                         "10"
                                                         "11"
                                                         "12"])
                                              (rand-nth ["01"
                                                         "02"
                                                         "03"
                                                         "04"
                                                         "05"
                                                         "06"
                                                         "07"
                                                         "08"
                                                         "09"
                                                         "10"
                                                         "11"
                                                         "12"])))
                      :address (rand-nth ["Fresno, CA"
                                          "Los Angeles, CA"
                                          "San Jose, CA"])
                      :phone_number "650 605 7491"})))
  (with-open [c (jdbc/get-connection (:db juxt.clip.repl/system))]
    (patients c))
  (with-open [c (jdbc/get-connection (:db juxt.clip.repl/system))]
    (update-patient c 1 {:name "foobar"}))
  (with-open [c (jdbc/get-connection (:db juxt.clip.repl/system))]
    (delete-patient c 1))


  )
