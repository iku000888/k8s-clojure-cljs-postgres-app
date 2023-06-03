(ns health-record-frontend.core
  (:require [ajax.core :refer [GET]]
            [helix.core :refer [defnc $]]
            [helix.hooks :as hooks]
            [helix.dom :as d]
            ["react-dom/client" :as rdom]
            ["smarthr-ui" :as shr]))

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

(defnc patient-form [{:keys [patient close]}]
  ($ shr/ActionDialog {:title (if (:patient_id patient)
                                "Update Patient"
                                "New Patient")
                       :isOpen (some? patient)
                       :width 800
                       :decorators #js {:closeButtonLabel (fn [r] "Close")}
                       :actionText "Submit"
                       :onClickClose close}
     ($ shr/Center
        ($ shr/DefinitionList
           {:items (clj->js  [{:term "Name"
                               :description ($ shr/Input)}
                              {:term "Gender"
                               :description ($ shr/Select {:value ""
                                                           :options (clj->js [{:label ""
                                                                               :value ""}
                                                                              {:label "Male"
                                                                               :value "Male"}
                                                                              {:label "Female"
                                                                               :value "Female"}])})}
                              {:term "Date of Birth"
                               :description ($ shr/DatePicker)}
                              {:term "Address"
                               :description ($ shr/Input)}
                              {:term "Phone number"
                               :description ($ shr/Input)}])}))
     ))

(defnc shr-table
  [{:keys []}]
  (let [[selected-patient set-selected-patient] (hooks/use-state nil)
        {:keys [patients selected-patient-ids select-patient-id
                toggle-select-all-patient-id]}
        (use-patients)]
    (d/div
     (pr-str selected-patient)
     (pr-str shr/Dialog)
     ($ shr/Table {}
        (d/thead
         ($ shr/BulkActionRow "salfkj")
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
                    ($ shr/Button {:onClick #(set-selected-patient p)}
                       "Edit"))))))
     ($ patient-form {:patient selected-patient
                      :close #(set-selected-patient nil)}))))

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
