(ns health-record-frontend.core
  (:require [helix.core :refer [$]]
            [health-record-frontend.page :refer [app]]
            ["react-dom/client" :as rdom]))


;; start your app with your favorite React renderer
(defonce root (rdom/createRoot (js/document.getElementById "app")))
(.render root ($ app))
