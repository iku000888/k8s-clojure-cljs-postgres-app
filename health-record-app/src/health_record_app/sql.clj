(ns health-record-app.sql
  (:require [next.jdbc.sql :as sql]
            [next.jdbc :as jdbc]
            [next.jdbc.types :refer [as-other]]
            [next.jdbc.date-time]
            [honey.sql :as h.sql]
            [honey.sql.helpers :as h]
            [clojure.string :as str]))

;; The as-other values are not interned and equality breaks
;; across every call making testing harder.
;; Making it static allows re-use of the same instance and maintains equality
(def ->sqlenum
  {"Male" (next.jdbc.types/as-other "Male")
   "Female" (next.jdbc.types/as-other "Female")})

(defn patients [conn {:strs [filter_logic name address phone_number gender dob_start dob_end]}]
  (let [where-clauses (cond-> []
                        name (conj [:like :name (str "%" (str/escape name {\% "\\%"}) "%")])
                        address (conj [:like :address (str "%" (str/escape address {\% "\\%"}) "%")])
                        phone_number (conj [:like :phone_number (str "%" (str/escape phone_number {\% "\\%"}) "%")])
                        gender (conj [:= :gender (->sqlenum gender)])
                        (and dob_start dob_end) (conj [:and
                                                       [:< :date_of_birth (java.time.LocalDate/parse dob_end)]
                                                       [:> :date_of_birth (java.time.LocalDate/parse dob_start)]])
                        (and (not dob_start) dob_end) (conj [:< :date_of_birth (java.time.LocalDate/parse dob_end)])
                        (and dob_start (not dob_end)) (conj [:> :date_of_birth (java.time.LocalDate/parse dob_start)]))]
    (sql/query conn
               (h.sql/format
                (cond-> (-> (h/select :*)
                            (h/from :patients))
                  (seq where-clauses) (h/where (into [(or (keyword filter_logic) :or)]
                                                     where-clauses))))
               {:builder-fn next.jdbc.result-set/as-unqualified-maps})))

(defn add-patient [conn patient]
  (sql/insert! conn :patients patient {:builder-fn next.jdbc.result-set/as-unqualified-maps}))

(defn update-patient [conn id patient]
  (sql/update! conn :patients patient {:patients/patient_id id}))

(defn delete-patient [conn id]
  (sql/delete! conn :patients {:patients/patient_id id}))

(comment
  (str/escape "%%%" {\% "\\%"})
  (let [where-clauses (cond-> []
                        name (conj [:like :name (str "%" (str/escape name {\% "\\%"}) "%")])
                        address (conj [:like :address (str "%" (str/escape address {\% "\\%"}) "%")])
                        phone_number (conj [:like :phone_number (str "%" (str/escape phone_number {\% "\\%"}) "%")])
                        gender (conj [:= :gender (as-other gender)])
                        (and dob_start dob_end) (conj [:and
                                                       [:< :date_of_birth (java.time.LocalDate/parse dob_end)]
                                                       [:> :date_of_birth (java.time.LocalDate/parse dob_start)]])
                        (and (not dob_start) dob_end) (conj [:< :date_of_birth (java.time.LocalDate/parse dob_end)])
                        (and dob_start (not dob_end)) (conj [:> :date_of_birth (java.time.LocalDate/parse dob_start)]))]
    (with-open [c (jdbc/get-connection (:db juxt.clip.repl/system))]
      (sql/query c
                 (h.sql/format
                  (cond-> (-> (h/select :*)
                              (h/from :patients))
                    (seq where-clauses) (h/where (into [(or (keyword filter_logic) :or)]
                                                       where-clauses)))))))



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
