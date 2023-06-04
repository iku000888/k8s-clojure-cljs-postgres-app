(ns health-record-frontend.core
  (:require [ajax.core :refer [GET PUT POST]]
            [helix.core :refer [defnc $]]
            [helix.hooks :as hooks]
            [helix.dom :as d]
            [goog.i18n.DateTimeFormat :as gdtf]
            ["react-dom/client" :as rdom]
            ["smarthr-ui" :as shr]))

(defn format-date [date]
  (let [year (.getFullYear date)
        month (.padStart (str (inc (.getMonth date)))
                         2 "0")
        day (.padStart (str (inc (.getDate date)))
                       2 "0") ]
    (str year "-" month "-" day)))

(defn use-patients []
  (let [[patients set-patients] (hooks/use-state {})
        [selected-patient-ids set-selected-patient-ids] (hooks/use-state #{})]
    (hooks/use-effect
     []
     (GET "http://localhost:8080/api/patients/"
          {:response-format :json
           :keywords? true
           :handler #(set-patients (->> %
                                        (map (fn [p]
                                               [(:patient_id p)
                                                p]))
                                        (into {})))}))

    {:patients patients
     :add-patient (fn [patient]
                    (POST "http://localhost:8080/api/patients/"
                          {:response-format :json
                           :format :json
                           :params patient
                           :keywords? true
                           :handler (fn [response]
                                      (set-patients
                                       #(assoc %
                                               (:patient_id response)
                                               response)))}))
     :update-patient (fn [id patient]
                       (PUT (str "http://localhost:8080/api/patients/" id)
                            {:response-format :json
                             :format :json
                             :params patient
                             :keywords? true
                             :handler (fn [_response]
                                        (set-patients
                                         #(assoc % id patient)))}))
     :selected-patient-ids selected-patient-ids
     :select-patient-id (fn [patient-id]
                          (set-selected-patient-ids
                           #(if (% patient-id)
                              (disj % patient-id)
                              (conj % patient-id))))
     :toggle-select-all-patient-id (fn []
                                     (set-selected-patient-ids
                                      #(if (empty? %)
                                         (set (keys patients))
                                         #{})))}))

(def theme (shr/createTheme))

(defnc greeting
  "A component which greets a user."
  [{:keys [name]}]
  ;; use helix.dom to create DOM elements
  (d/div {} "Hello, " (d/strong name) "!"))

(defn valid-phone-number? [phone]
  (when phone
    (js/console.log
     (pr-str [phone
              (re-matches #"\(?([2-9][0-8][0-9])\)?[-. ]?([2-9][0-9]{2})[-. ]?([0-9]{4})"
                          phone)])))
  (or (empty? phone)
      (not (empty? (re-matches #"\(?([2-9][0-8][0-9])\)?[-. ]?([2-9][0-9]{2})[-. ]?([0-9]{4})"
                               phone)))))

(defnc patient-form [{:keys [patient close update-patient add-patient]}]
  (let [[edited-patient set-edited-patient] (hooks/use-state patient)]
    ($ shr/ActionDialog {:title (if (:patient_id patient)
                                  "Update Patient"
                                  "New Patient")
                         :isOpen (some? patient)
                         :onClickAction (fn []
                                          (if (:patient_id edited-patient)
                                            (update-patient (:patient_id edited-patient)
                                                            edited-patient)
                                            (add-patient edited-patient)))
                         :width 800
                         :decorators #js {:closeButtonLabel (fn [r] "Close")}
                         :actionText "Submit"
                         :onClickClose close}
       ($ shr/Center
          ($ shr/DefinitionList
             {:className "patient-form-container"
              :layout "single"
              :items (clj->js  [{:term "Name"
                                 :description ($ shr/Input {:error (empty? (:name edited-patient))
                                                            :value (:name edited-patient)
                                                            :onChange #(set-edited-patient
                                                                        (fn [ep]
                                                                          (assoc ep :name (.-value (.-target %)))))})}
                                {:term "Gender"
                                 :description ($ shr/Select {:error (empty? (:gender edited-patient))
                                                             :value (:gender edited-patient)
                                                             :onChange #(set-edited-patient
                                                                         (fn [ep]
                                                                           (assoc ep :gender (.-value (.-target %)))))
                                                             :options (clj->js [{:label ""
                                                                                 :value ""}
                                                                                {:label "Male"
                                                                                 :value "Male"}
                                                                                {:label "Female"
                                                                                 :value "Female"}])})}
                                {:term "Date of Birth"
                                 :description ($ shr/DatePicker {:value (:date_of_birth edited-patient)
                                                                 :formatDate format-date
                                                                 :onChangeDate #(set-edited-patient
                                                                                 (fn [ep]
                                                                                   (assoc ep :date_of_birth %2)))})}
                                {:term "Address"
                                 :description ($ shr/Input {:value (:address edited-patient)
                                                            :onChange #(set-edited-patient
                                                                        (fn [ep]
                                                                          (assoc ep :address (.-value (.-target %)))))})}
                                {:term "Phone number (xxx xxx xxxx)"
                                 :description ($ shr/Input {:error (not (valid-phone-number? (:phone_number edited-patient)))
                                                            :defaultValue (:phone_number edited-patient)
                                                            :onChange #(set-edited-patient
                                                                        (fn [ep]
                                                                          (assoc ep :phone_number (.-value (.-target %)))))})}])})))))

(defnc shr-table
  [{:keys []}]
  (let [[selected-patient set-selected-patient] (hooks/use-state nil)
        {:keys [patients selected-patient-ids select-patient-id
                toggle-select-all-patient-id
                update-patient
                add-patient]}
        (use-patients)]
    (d/div
     (pr-str selected-patient)
     (pr-str shr/Dialog)
     ($ shr/Table {}
        (d/thead
         ($ shr/BulkActionRow
            (when (seq selected-patient-ids)
              ($ shr/Button {:onClick #(set-selected-patient (constantly {}))}
                 "Delete Selected"))
            ($ shr/Button {:onClick #(set-selected-patient (constantly {}))}
               "New Patient"))
         (d/tr
          ($ shr/ThCheckbox {:onClick toggle-select-all-patient-id
                             :checked (= selected-patient-ids (set (keys patients)))})
          ($ shr/Th "Name")
          ($ shr/Th "Gender")
          ($ shr/Th "Date of birth")
          ($ shr/Th "Address")
          ($ shr/Th "Phone number")
          ($ shr/Th "")))
        (d/tbody
         (for [[id p] patients]
           (d/tr {:key id}
                 ($ shr/TdCheckbox {:onClick #(select-patient-id id)
                                    :checked (some? (selected-patient-ids id))})
                 ($ shr/Td
                    (:name p))
                 ($ shr/Td
                    (:gender p))
                 ($ shr/Td
                    (:date_of_birth p))
                 ($ shr/Td
                    (:address p))
                 ($ shr/Td
                    (:phone_number p))
                 ($ shr/Td
                    ($ shr/Button {:onClick #(set-selected-patient (constantly p))}
                       "Edit"))))))
     (when selected-patient
       ($ patient-form {:patient selected-patient
                        :add-patient (fn [patient]
                                       (add-patient patient)
                                       (set-selected-patient nil))
                        :update-patient (fn [id patient]
                                          (update-patient id patient)
                                          (set-selected-patient nil))
                        :close #(set-selected-patient nil)})))))

(defnc app []
  #_($ shr/ThemeProvider {:theme theme}
       ($ shr/Button {:variant "primary"}
          "Hello world"))
  #_($ greeting {})
  ($ shr-table {})
  #_($ shr/Table {}
       (d/thead {})
       #_($ shr/EmptyTableBody {}
            "fuu"))
  #_($ d/div {}
       "foo"))

;; start your app with your favorite React renderer
(defonce root (rdom/createRoot (js/document.getElementById "app")))
(.render root ($ app))
