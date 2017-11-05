(ns re-frame-request.core
  (:require [cljs.spec.alpha :as s]
            [re-frame-request.events :as rfr-events]
            [re-frame-request.subscriptions :as rfr-subscriptions]))

(def initial-state
  "Initial re-frame-request state.

  All requests are associated into a map."
  {})


;; Specs for each individual request
(s/def ::status #{:loading :success :failure})
(s/def ::request-time number?)
(s/def ::error (s/nilable map?))
(s/def ::request (s/map-of keyword? (s/keys :req-un [::status
                                                     ::request-time
                                                     ::error])))

(defn register-events
  "Registers re-frame-request events and request handler"
  [opts]
  (rfr-events/register-events opts))

(defn register-subscriptions
  "Registers re-frame-request subscriptions"
  []
  (rfr-subscriptions/register-subscriptions))

(defn register-all
  "Registers both re-frame-request events & subscriptions"
  [{:keys [event-options]}]
  (register-events event-options)
  (register-subscriptions))
