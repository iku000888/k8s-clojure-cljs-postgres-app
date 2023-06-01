(ns dev
  (:require
   [health-record-app.core :as core]
   [juxt.clip.repl :refer [start stop reset set-init! system]]))

(set-init! core/config)
