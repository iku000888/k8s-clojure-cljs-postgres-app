(ns health-record-app.core
  (:require [aleph.http :as http]
            [yada.yada :as yada]
            [juxt.clip.edn :as clip.edn]
            [clojure.java.io :as io]
            [clojure.edn :as edn]
            [aero.core :refer [read-config]]))

(defn config []
  (clip.edn/load
   (edn/read-string
    (slurp
     (io/resource "config.edn")))))

(defn foo
  "I don't do a whole lot."
  [x]
  (println x "Hello, World!"))
