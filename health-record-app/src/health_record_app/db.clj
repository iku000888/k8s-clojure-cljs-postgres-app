(ns health-record-app.db
  (:require [next.jdbc :as jdbc]
            [next.jdbc.sql :as sql]
            [next.jdbc.connection :as connection]
            [clojure.string :as str])
  (:import (com.zaxxer.hikari HikariDataSource)))

(defn setup-schema [db]
  (with-open [c (jdbc/get-connection db)]
    (when-not (seq (sql/query c
                              ["SELECT 1 FROM pg_type WHERE typname = 'gender_type'"]))
      (jdbc/execute! c
                     ["CREATE TYPE gender_type AS ENUM ('Male', 'Female', 'Other');"]))
    (when-not (seq (sql/query c
                              ["SELECT 1 FROM information_schema.tables WHERE table_name = 'patients'"]))
      (jdbc/execute! c
                     [(str/join
                       "\n"
                       ["CREATE TABLE patients ("
                        "patient_id SERIAL PRIMARY KEY,"
                        "name VARCHAR(255) NOT NULL,"
                        "gender gender_type NOT NULL,"
                        "date_of_birth DATE NOT NULL,"
                        "address VARCHAR(255),"
                        "phone_number VARCHAR(20)"
                        ")"])]))))

(defn pool [config]
  (let [pool (connection/->pool com.zaxxer.hikari.HikariDataSource
                                {:jdbcUrl (connection/jdbc-url config)})]
    (setup-schema pool)
    pool))

(defn pool.stop [pool]
  (.close pool))
