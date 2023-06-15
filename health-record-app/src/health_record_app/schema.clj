(ns health-record-app.schema
  (:require [malli.core :as m]
            [malli.registry :as mr]
            [malli.error :as me]
            [malli.experimental.time :as met]))

(mr/set-default-registry!
 (mr/composite-registry
  (m/default-schemas)
  (met/schemas)))

(def Patient
  [:map
   [:name string?]
   [:date_of_birth
    [:fn {:error/message "Date format is yyyy-MM-dd"}
     #(java.time.LocalDate/parse %)]]
   [:gender [:enum "Male" "Female"]]
   [:address {:optional true}
    string?]
   [:phone_number {:optional true}
    [:re {:error/message "Invalid phone number format"}
     #"\(?([2-9][0-8][0-9])\)?[-. ]?([2-9][0-9]{2})[-. ]?([0-9]{4})"]]])

(def PatientSchema
  (m/schema Patient))
