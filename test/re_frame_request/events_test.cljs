(ns re-frame-request.events-test
  (:require [cljs.test :refer-macros [deftest is testing]]

            [re-frame-request.events :refer [request-start
                                             request-done
                                             request-reset]]))

(def db-0 {})

(deftest request-lifecycle
  (let [db-1 (request-start db-0 [nil {:name :users/get-users
                                       :request-time 1500000000000}])
        db-2 (request-start db-1 [nil {:name :orders/delete-order
                                       :request-time 1500000000000}])
        db-3 (request-done db-2 [nil {:name :users/delete-order
                                      :request-time 1500000000000
                                      :error nil
                                      :status 200}])
        db-4 (request-done db-3 [nil {:name :users/get-users
                                      :request-time 1500000000000
                                      :error {:message "Unauthorized"}
                                      :status 401}])]
    (= db-1 {:request {:users/get-users {:status :loading
                                         :request-time 1500000000000
                                         :error nil}}})))
