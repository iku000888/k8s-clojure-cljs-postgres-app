(ns health-record-app.db
  (:require [next.jdbc :as jdbc]
            [next.jdbc.sql :as sql]
            [next.jdbc.connection :as connection])
  (:import (com.zaxxer.hikari HikariDataSource)))

(defn pool [config]
  (connection/->pool com.zaxxer.hikari.HikariDataSource
                     {:jdbcUrl (connection/jdbc-url config)}))

(defn pool.stop [pool]
  (.close pool))
