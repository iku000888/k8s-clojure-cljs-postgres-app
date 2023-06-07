(ns health-record-frontend.pact-test
  (:require [cljs.test :as t :refer-macros [async]]
            ["@testing-library/react" :as rt]
            ["@testing-library/user-event$default" :as userEvent]
            [helix.core :refer [$]]
            [helix.dom :as d]
            [health-record-frontend.page :as page]
            [cljs.core.async :refer [go <!]]
            [cljs.core.async.interop :refer-macros [<p!]]))

(t/deftest patient-pact-test
  (rt/render ($ page/app "foo"))
  (async done
         (go
           (t/testing "New patient flow sends compliant POST request"
             (<p! (.click userEvent
                          (.getByText rt/screen "New Patient")))
             (<p! (rt/waitFor (fn [] (.getByLabelText rt/screen "Name"))
                              #js {:timeout 10000}))
             (<p! (.selectOptions userEvent
                                  (.getByLabelText rt/screen "Gender")
                                  "Female"))
             (<p! (.type userEvent
                         (.getByLabelText rt/screen "Name")
                         "Jimmy Jon"))
             (<p! (.type userEvent
                         (.getByLabelText rt/screen "Date of Birth")
                         "2000-03-27"))
             (<p! (.type userEvent
                         (.getByLabelText rt/screen "Address")
                         "123 Block B, CA"))
             (<p! (.type userEvent
                         (.getByLabelText rt/screen "Phone number (xxx xxx xxxx)")
                         "333 444 9999"))
             (<p! (.click userEvent
                          (.getByText rt/screen "Submit")))
             (t/is (<p! (rt/waitFor (fn [] (.getByText rt/screen "Request Success"))
                                    #js {:timeout 10000})))
             (<p! (.click userEvent
                          (.getByText rt/screen "Dismiss"))))

           (t/testing "Edit patient flow sends compliant PUT request"
             (<p! (.click userEvent
                          (.getByText rt/screen "Edit")))
             (<p! (rt/waitFor (fn [] (.getByLabelText rt/screen "Name"))
                              {:timeout 10000}))
             (<p! (.selectOptions userEvent
                                  (.getByLabelText rt/screen "Gender")
                                  "Male"))
             (<p! (.clear userEvent
                          (.getByLabelText rt/screen "Name")))
             (<p! (.type userEvent
                         (.getByLabelText rt/screen "Name")
                         "Limmy Lon"))
             (<p! (.clear userEvent
                          (.getByLabelText rt/screen "Date of Birth")))
             (<p! (.type userEvent
                         (.getByLabelText rt/screen "Date of Birth")
                         "2001-03-27"))
             (<p! (.clear userEvent
                          (.getByLabelText rt/screen "Address")))
             (<p! (.type userEvent
                         (.getByLabelText rt/screen "Address")
                         "123 Block C, CA"))
             (<p! (.clear userEvent
                          (.getByLabelText rt/screen "Phone number (xxx xxx xxxx)")))
             (<p! (.type userEvent
                         (.getByLabelText rt/screen "Phone number (xxx xxx xxxx)")
                         "333 444 9998"))
             (<p! (.click userEvent
                          (.getByText rt/screen "Submit")))
             (t/is (<p! (rt/waitFor (fn [] (.getByText rt/screen "Request Success"))
                                    #js {:timeout 20000})))
             (<p! (.click userEvent
                          (.getByText rt/screen "Dismiss"))))
           ;;(<! (new-patient))
           #_(<p! (update-patient))


           #_(t/testing "Delete patient flow sends compliant DELETE request"
               (<p! (.click userEvent
                            (js/document.querySelector "#check-all")))
               )
           #_(js/alert "break")

           (done)
           (rt/cleanup)))





  )
