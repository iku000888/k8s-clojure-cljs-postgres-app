(ns steps-definitions.app-steps
  (:require [lambdaisland.cucumber.dsl :refer :all]
            [clojure.test :refer :all]
            [aleph.http :as http]
            [cheshire.core :as che]
            ))

(Given "this api endpoint (.*)" [state endpoint]
  (assoc state
         :get-patients (fn []
                         (-> @(http/get (format "%s/api/patients/" endpoint))
                             (:body)
                             (slurp)
                             (che/decode keyword)))
         :update-patient (fn [id payload]
                           (-> @(http/put (format "%s/api/patients/%s" endpoint id)
                                          {:headers {"Content-Type" "application/json"}
                                           :body (che/generate-string payload)})
                               (:body)
                               (slurp)
                               (che/decode keyword)))
         :delete-patient (fn [id] @(http/delete (format "%s/api/patients/%s" endpoint id)))
         :new-patient (fn [payload]
                        (-> @(http/post
                              (format "%s/api/patients/" endpoint)
                              {:headers {"Content-Type" "application/json"}
                               :body (che/generate-string payload)})
                            (:body )
                            (slurp)
                            (che/decode keyword)))))

(Given "the following patient information" [state table]
  (assoc state
         :patient-information
         (into {}
               (map (fn [[name gender dob address phone]]
                      [name {:name name
                             :gender gender
                             :date_of_birth dob
                             :address address
                             :phone_number phone}]))
               table)))

(When "I add (.*) to the system" [state patient]
  (assoc state
         :patient patient
         :add-response ((:new-patient state)
                        (get-in state [:patient-information patient]))))

(And "Get the patient list" [state]
  ;; Write code here that turns the phrase above into concrete actions
  (assoc state :patient-list ((:get-patients state))))

(Then "(.*) is in the list" [state patient]
  ;; Write code here that turns the phrase above into concrete actions
  (let [all-patients (->> (:patient-list state)
                          (map (juxt :name identity))
                          (into {}))]
    (is (contains? all-patients patient))
    (is (= (dissoc (get all-patients patient) :patient_id)
           (get-in state [:patient-information patient])))
    state))

(When "I update (.*) in the system with (.*)'s information" [state old-patient new-patient]
  (let [add-response ((:new-patient state)
                      (get-in state [:patient-information old-patient]))]
    (assoc state
           :patient-list-before-update ((:get-patients state))
           :add-response add-response
           :update-response ((:update-patient state)
                             (:patient_id add-response)
                             (get-in state [:patient-information new-patient])))))

(And "Get the patient list after updating" [state]
  ;; Write code here that turns the phrase above into concrete actions
  (assoc state :patient-list-after-update ((:get-patients state))))

(Then "(.*)'s information in the new list is updated with (.*)" [state
                                                                 old-patient
                                                                 new-patient]
  (let [all-patients (->> (:patient-list-after-update state)
                          (map (juxt :patient_id identity))
                          (into {}))]
    (is (contains? all-patients (get-in state [:add-response :patient_id])))
    (is (= (dissoc (get all-patients (get-in state [:add-response :patient_id]))
                   :patient_id)
           (get-in state [:patient-information new-patient])))
    state))

(When "I delete (.*) in the system" [state patient-to-delete]
  (let [add-response ((:new-patient state)
                      (get-in state [:patient-information patient-to-delete]))]
    (assoc state
           :patient-list-before-update ((:get-patients state))
           :add-response add-response
           :delete-response ((:delete-patient state)
                             (:patient_id add-response)))))

(And "Get the patient list after deleting" [state]
  (assoc state :patient-list-after-update ((:get-patients state))))

(Then "(.*)'s information in the new list is gone" [state old-patient]
  (let [old-patients (->> (:patient-list-before-update state)
                          (map (juxt :patient_id identity))
                          (into {}))
        all-patients (->> (:patient-list-after-update state)
                          (map (juxt :patient_id identity))
                          (into {}))]
    (is (contains? old-patients (get-in state [:add-response :patient_id])))
    (is (not (contains? all-patients (get-in state [:add-response :patient_id]))))
    state))
