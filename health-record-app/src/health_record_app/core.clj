(ns health-record-app.core
  (:gen-class)
  (:require [aleph.http :as http]
            [dialog.logger :as log]
            [yada.yada :as yada]
            [juxt.clip.core :as clip]
            [juxt.clip.edn :as clip.edn]
            [clojure.java.io :as io]
            [aero.core :refer [read-config]]))


(defn config []
  (log/initialize!)
  (clip.edn/load
   (read-config
    (io/resource "config.edn"))))

(def system nil)

(defn -main
  [& _]
  (let [system-config (config)
        system (clip/start system-config)]
    (alter-var-root #'system (constantly system))
    (.addShutdownHook
     (Runtime/getRuntime)
     (Thread. #(clip/stop system-config system))))
  @(promise))
