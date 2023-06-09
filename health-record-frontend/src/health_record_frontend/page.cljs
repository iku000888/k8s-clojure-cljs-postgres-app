(ns health-record-frontend.page
  (:require [ajax.core :refer [GET PUT POST DELETE
                               default-interceptors to-interceptor]]
            [clojure.string :as str]
            [health-record-frontend.page :refer [app]]
            [helix.core :refer [defnc $]]
            [helix.hooks :as hooks]
            [helix.dom :as d]
            [goog.i18n.DateTimeFormat :as gdtf]
            ["react-dom/client" :as rdom]
            ["smarthr-ui" :as shr]))

(defn delete-is-empty [{:keys [method] :as request}]
  (if (= method "DELETE")
    (reduced (assoc request :body nil))
    request))

(def app-engine-delete-interceptor
  (to-interceptor {:name "Google App Engine Delete Rule"
                   :request delete-is-empty}))

;;; Since this rule uses `reduced`, it is important that it is
;;; positioned at the end of the list, hence we use `concat` here
(swap! default-interceptors concat [app-engine-delete-interceptor])

(defn format-date [date]
  (let [year (.getFullYear date)
        month (.padStart (str (inc (.getMonth date)))
                         2 "0")
        day (.padStart (str (inc (.getDate date)))
                       2 "0") ]
    (str year "-" month "-" day)))

(defn use-patients []
  (let [[patients set-patients] (hooks/use-state {})
        [selected-patient-ids set-selected-patient-ids] (hooks/use-state #{})
        [request-error set-request-error] (hooks/use-state nil)
        [request-success set-request-success] (hooks/use-state nil)]
    (hooks/use-effect
     []
     (GET "http://localhost:8080/api/patients/"
          {:response-format :json
           :keywords? true
           :handler #(set-patients (->> %
                                        (map (fn [p]
                                               [(:patient_id p)
                                                p]))
                                        (into {})))
           :error-handler #(set-request-error %)}))
    (hooks/use-effect
     [request-success]
     (when request-success
       (js/setTimeout (fn [] (set-request-success nil)) 6000)))
    {:patients patients
     :add-patient (fn [patient {:keys [on-success on-failure]}]
                    (POST "http://localhost:8080/api/patients/"
                          {:response-format :json
                           :format :json
                           :params patient
                           :keywords? true
                           :error-handler on-failure
                           :handler (fn [response]
                                      (set-patients
                                       #(assoc %
                                               (:patient_id response)
                                               response))
                                      (on-success)
                                      (set-request-success "Successfully added patient"))}))
     :update-patient (fn [id patient {:keys [on-success on-failure]}]
                       (PUT (str "http://localhost:8080/api/patients/" id)
                            {:response-format :json
                             :format :json
                             :params patient
                             :keywords? true
                             :error-handler on-failure
                             :handler (fn [_response]
                                        (set-request-success "Successfully updated patient")
                                        (on-success)
                                        (set-patients
                                         #(assoc % id patient)))}))
     :delete-patient (fn [id]
                       (DELETE (str "http://localhost:8080/api/patients/" id)
                               {:response-format :json
                                :keywords? true
                                :body nil
                                :error-handler #(set-request-error %)
                                :handler (fn [_response]
                                           (set-selected-patient-ids #(disj % id))
                                           (set-request-success "Successfully deleted patient")
                                           (set-patients
                                            #(dissoc % id)))}))
     :selected-patient-ids selected-patient-ids
     :select-patient-id (fn [patient-id]
                          (set-selected-patient-ids
                           #(if (% patient-id)
                              (disj % patient-id)
                              (conj % patient-id))))
     :toggle-select-all-patient-id (fn [filtered-patients]
                                     (set-selected-patient-ids
                                      #(if (empty? %)
                                         (set (if (seq filtered-patients)
                                                (keys filtered-patients)
                                                (keys patients)))
                                         #{})))
     :request-success request-success
     :set-request-success set-request-success
     :request-error request-error
     :set-request-error set-request-error
     :query-patients (fn [query]
                       (GET (str "http://localhost:8080/api/patients/"
                                 "?"
                                 (str/join "&"
                                           (map (fn [[k v]]
                                                  (str (name k) "=" v))
                                                query)))
                            {:response-format :json
                             :keywords? true
                             :handler #(set-patients (->> %
                                                          (map (fn [p]
                                                                 [(:patient_id p)
                                                                  p]))
                                                          (into {})))
                             :error-handler #(set-request-error %)}))}))

(def theme (shr/createTheme))

(defn valid-phone-number? [phone]
  (or (empty? phone)
      ;; just made chat gpt produce something
      (not (empty? (re-matches #"\(?([2-9][0-8][0-9])\)?[-. ]?([2-9][0-9]{2})[-. ]?([0-9]{4})"
                               phone)))))

(defnc search-form [{:keys [submit-query query close update-patient]}]
  (let [[edited-query set-edited-query] (hooks/use-state query)]
    ($ shr/ActionDialog {:title "Advance Search"
                         :isOpen (some? query)
                         :onClickAction (fn []
                                          (submit-query edited-query)
                                          (close))
                         :width 800
                         :decorators #js {:closeButtonLabel (fn [r] "Close")}
                         :actionText "Submit"
                         :onClickClose close}
       ($ shr/Center
          (pr-str edited-query)
          ($ shr/DefinitionList
             {:className "patient-form-container"
              :layout "single"
              :items (clj->js  [{:description ($ shr/FormControl {:title "Filter Logic"
                                                                  :className "filter-logic"}
                                                 ($ shr/Select {:name "Filter Logic"
                                                                :value (:filter_logic edited-query)
                                                                :onChange #(set-edited-query
                                                                            (fn [ep]
                                                                              (assoc ep :filter_logic (.-value (.-target %)))))
                                                                :options (clj->js [{:label ""
                                                                                    :value nil}
                                                                                   {:label "and"
                                                                                    :value "and"}
                                                                                   {:label "or"
                                                                                    :value "or"}])}))}
                                {:description
                                 ($ shr/FormControl {:title "Name"}
                                    ($ shr/Input {:name "Name"
                                                  :value (:name edited-query "")
                                                  :onChange #(set-edited-query
                                                              (fn [ep]
                                                                (assoc ep :name (.-value (.-target %)))))}))}
                                {:description ($ shr/FormControl {:title "Gender"
                                                                  :className "gender-input"}
                                                 ($ shr/Select {:name "Gender"
                                                                :value (:gender edited-query "")
                                                                :onChange #(set-edited-query
                                                                            (fn [ep]
                                                                              (assoc ep :gender (.-value (.-target %)))))
                                                                :options (clj->js [{:label ""
                                                                                    :value ""}
                                                                                   {:label "Male"
                                                                                    :value "Male"}
                                                                                   {:label "Female"
                                                                                    :value "Female"}])}))}
                                {:description ($ shr/FormControl {:title "Date of Birth Range"}
                                                 "from"

                                                 ($ shr/DatePicker {:name "Date of Birth"
                                                                    :value (:dob_start edited-query "")
                                                                    :formatDate format-date
                                                                    :onChangeDate #(set-edited-query
                                                                                    (fn [ep]
                                                                                      (assoc ep :dob_start %2)))})

                                                 "to"
                                                 ($ shr/DatePicker {:name "Date of Birth"
                                                                    :value (:dob_end edited-query "")
                                                                    :formatDate format-date
                                                                    :onChangeDate #(set-edited-query
                                                                                    (fn [ep]
                                                                                      (assoc ep :dob_end %2)))}))}
                                {:description ($ shr/FormControl {:title "Address"}
                                                 ($ shr/Input {:name "Address"
                                                               :value (:address edited-query "")
                                                               :onChange #(set-edited-query
                                                                           (fn [ep]
                                                                             (assoc ep :address (.-value (.-target %)))))}))}
                                {:description ($ shr/FormControl {:title "Phone number (xxx xxx xxxx)"}
                                                 ($ shr/Input {:name "Phone"
                                                               :defaultValue (:phone_number edited-query)
                                                               :onChange #(set-edited-query
                                                                           (fn [ep]
                                                                             (assoc ep :phone_number (.-value (.-target %)))))}))}])})))))

(defnc patient-form [{:keys [patient close update-patient add-patient submit-failure]}]
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
                         :actionDisabled (not (seq (:name edited-patient)))
                         :decorators #js {:closeButtonLabel (fn [r] "Close")}
                         :actionText (if (not (seq (:name edited-patient)))
                                       "Please fix input"
                                       "Submit")
                         :onClickClose close}
       ($ shr/Center
          ($ shr/DefinitionList
             {:className "patient-form-container"
              :layout "single"
              :items (clj->js  [{:description
                                 ($ shr/FormControl {:title "Name"
                                                     :errorMessages (get-in submit-failure [:response :name])}
                                    ($ shr/Input {:id "patient-form-name"
                                                  :name "Name"
                                                  :error (empty? (:name edited-patient))
                                                  :value (:name edited-patient "")
                                                  :onChange #(set-edited-patient
                                                              (fn [ep]
                                                                (assoc ep :name (.-value (.-target %)))))}))}
                                {:description ($ shr/FormControl {:title "Gender"
                                                                  :className "gender-input"
                                                                  :errorMessages (get-in submit-failure [:response :gender])}
                                                 ($ shr/Select {:id "patient-form-gender"
                                                                :name "Gender"
                                                                :error (empty? (:gender edited-patient))
                                                                :value (:gender edited-patient "")
                                                                :onChange #(set-edited-patient
                                                                            (fn [ep]
                                                                              (assoc ep :gender (.-value (.-target %)))))
                                                                :options (clj->js [{:label ""
                                                                                    :value ""}
                                                                                   {:label "Male"
                                                                                    :value "Male"}
                                                                                   {:label "Female"
                                                                                    :value "Female"}])}))}
                                {:description ($ shr/FormControl {:title "Date of Birth"
                                                                  :errorMessages (get-in submit-failure [:response :date_of_birth])}
                                                 ($ shr/DatePicker {:id "patient-form-dob"
                                                                    :name "Date of Birth"
                                                                    :value (:date_of_birth edited-patient "")
                                                                    :formatDate format-date
                                                                    :onChangeDate #(set-edited-patient
                                                                                    (fn [ep]
                                                                                      (assoc ep :date_of_birth %2)))}))}
                                {:description ($ shr/FormControl {:title "Address"
                                                                  :errorMessages (get-in submit-failure [:response :address])}
                                                 ($ shr/Input {:id "patient-form-address"
                                                               :name "Address"
                                                               :value (:address edited-patient "")
                                                               :onChange #(set-edited-patient
                                                                           (fn [ep]
                                                                             (assoc ep :address (.-value (.-target %)))))}))}
                                {:description ($ shr/FormControl {:title "Phone number (xxx xxx xxxx)"
                                                                  :errorMessages (get-in submit-failure [:response :phone_number])}
                                                 ($ shr/Input {:id "patient-form-phone"
                                                               :name "Phone"
                                                               :error (not (valid-phone-number? (:phone_number edited-patient)))
                                                               :defaultValue (:phone_number edited-patient)
                                                               :onChange #(set-edited-patient
                                                                           (fn [ep]
                                                                             (assoc ep :phone_number (.-value (.-target %)))))}))}])})))))

(defnc shr-table
  [{:keys []}]
  (let [[filter-input set-filter-input] (hooks/use-state nil)
        [selected-patient set-selected-patient] (hooks/use-state nil)
        [search-query set-search-query] (hooks/use-state nil)
        [submit-failure set-submit-failure] (hooks/use-state nil)
        {:keys [patients selected-patient-ids select-patient-id
                toggle-select-all-patient-id
                update-patient
                add-patient
                set-request-error
                set-request-success
                delete-patient
                request-error
                request-success
                query-patients]}
        (use-patients)
        filtered-patients (if-not (empty? filter-input)
                            (->> patients
                                 (filter (fn [[id p]]
                                           (let [ps (vals p)]
                                             (some #(when (and filter-input %)
                                                      (str/includes?
                                                       (str/lower-case (str %))
                                                       (str/lower-case filter-input)))
                                                   ps))))
                                 (into {}))
                            patients)]
    (d/div
     ($ shr/Table {:fixedHead true}
        (d/thead
         (cond
           request-error
           ($ shr/BulkActionRow
              ($ shr/InformationPanel {:title "Request Failed"
                                       :decorators #js {:closeButtonLabel (fn [r] "Dismiss")}
                                       :onClickTrigger (fn []
                                                         (set-request-error nil))
                                       :type "error"}
                 (:message (:response request-error))))

           request-success
           ($ shr/BulkActionRow
              ($ shr/InformationPanel {:id "success-message"
                                       :title "Request Success"
                                       :decorators #js {:closeButtonLabel (fn [r] "Dismiss")}
                                       :onClickTrigger (fn []
                                                         (set-request-success nil))
                                       :type "success"}
                 request-success))

           :else
           ($ shr/BulkActionRow
              ($ shr/SearchInput {:id "search-input"
                                  :value (or filter-input "")
                                  :tooltipMessage "Filter by space delimited Name, Address, Phone number or DOB"
                                  :onChange #(set-filter-input
                                              (.-value (.-target %)))})
              (when (seq selected-patient-ids)
                ($ shr/Button {:onClick (fn []
                                          (doseq [sel-id selected-patient-ids]
                                            (delete-patient sel-id)))}
                   "Delete Selected"))
              (when-not (or (seq selected-patient-ids)
                            (seq filter-input))
                ($ shr/Button {:onClick #(set-selected-patient (constantly {:name ""
                                                                            :gender ""
                                                                            :phone_number ""
                                                                            :address ""}))}
                   "New Patient"))
              (when-not (or (seq selected-patient-ids)
                            (seq filter-input))
                ($ shr/Button {:variant "primary"
                               :prefix ($ shr/FaSearchIcon)

                               :onClick #(set-search-query (constantly {}))}
                   "Advanced Query"))))

         (d/tr
          ($ shr/ThCheckbox {:id "check-all"
                             :onClick #(toggle-select-all-patient-id
                                        (when-not(empty? filter-input)
                                          filtered-patients))
                             :checked (= selected-patient-ids (set (keys patients)))})
          ($ shr/Th "Name")
          ($ shr/Th "Gender")
          ($ shr/Th "Date of birth")
          ($ shr/Th "Address")
          ($ shr/Th "Phone number")
          ($ shr/Th "")))
        (d/tbody
         (for [[id p] filtered-patients]
           (d/tr {:key id}
                 ($ shr/TdCheckbox {:id "check-one"
                                    :onClick #(select-patient-id id)
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
                                       (add-patient patient
                                                    {:on-success #(set-selected-patient nil)
                                                     :on-failure #(set-submit-failure %)}))
                        :update-patient (fn [id patient]
                                          (update-patient id patient
                                                          {:on-success #(set-selected-patient nil)
                                                           :on-failure #(set-submit-failure %)}))
                        :submit-failure submit-failure
                        :close #(set-selected-patient nil)}))
     ($ search-form {:query search-query
                     :submit-query query-patients
                     :close #(set-search-query nil)}))))

(defnc app []
  ($ shr/Stack
     ($ shr/AppNavi {:label "Health Tek"})
     ($ shr-table {})))
