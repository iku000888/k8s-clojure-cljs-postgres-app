(ns health-record-frontend.pact-test
  (:require [cljs.test :as t :refer-macros [async]]
            ["@testing-library/react" :as rt]
            ["@testing-library/user-event$default" :as userEvent]
            [helix.core :refer [$]]
            [helix.dom :as d]
            [health-record-frontend.page :as page]
            [cljs.core.async :refer [go]]
            [cljs.core.async.interop :refer-macros [<p!]]))

(t/deftest foo-test
  (async done
         (go
           (rt/render ($ page/app "foo"))
           (<p! (.click userEvent
                        (.getByText rt/screen "New Patient")))
           (<p! (.type userEvent
                       (.getByLabelText rt/screen "Name")
                       "Foobar"))
           (<p! (.selectOptions userEvent
                                (.getByLabelText rt/screen "Gender")
                                "Male"))
           (<p! (.type userEvent
                       (.getByLabelText rt/screen "Date of Birth")
                       "2010-03-04"))
           (<p! (.type userEvent
                       (.getByLabelText rt/screen "Address")
                       "Foobar"))
           (<p! (.type userEvent
                       (.getByLabelText rt/screen "Phone number (xxx xxx xxxx)")
                       "Foobar"))
           (<p! (.click userEvent
                        (.getByText rt/screen "Submit")))

           (<p! (.click userEvent
                        (.getByText rt/screen "New Patient")))
           (js/alert "gar!")
           (t/is (= 1 1))

           #_(rt/cleanup)

           (done)))


  )
