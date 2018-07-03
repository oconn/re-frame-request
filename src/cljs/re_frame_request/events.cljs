(ns re-frame-request.events
  (:require [ajax.core :as ajax]
            [goog.net.ErrorCode :as errors]
            [re-frame.core :refer [dispatch
                                   reg-event-db
                                   reg-fx]]))

(defn ajax-xhrio-handler
  {:attribution "https://github.com/Day8/re-frame-http-fx"
   :doc "ajax-request only provides a single handler for success and errors"}
  [on-success on-failure xhrio [success? response]]
  (if success?
    (on-success response)
    (let [details (merge
                   {:uri             (.getLastUri xhrio)
                    :last-method     (.-lastMethod_ xhrio)
                    :last-error      (.getLastError xhrio)
                    :last-error-code (.getLastErrorCode xhrio)
                    :debug-message   (-> xhrio .getLastErrorCode (errors/getDebugMessage))}
                   response)]
      (on-failure details))))

(defn request->xhrio-options
  {:attribution "https://github.com/Day8/re-frame-http-fx"
   :doc "Formats the xhr options"}
  [{:as   request
    :keys [on-success on-failure]}]
  (let [api (new js/goog.net.XhrIo)]
    (-> request
        (assoc
         :api     api
         :handler (partial ajax-xhrio-handler
                           #(on-success %)
                           #(on-failure %)
                           api))
        (dissoc :on-success :on-failure))))

(defn wrap-success!
  "Wraps the passed in on-success callback"
  [on-success name request-time]
  (fn [response]
    (dispatch [:request/done
               {:status :success
                :name name
                :error nil
                :request-time request-time}])
    (dispatch (conj on-success response))))

(defn wrap-failure!
  "Wraps the passed in on-failure callback"
  [on-failure name request-time]
  (fn [error]
    (dispatch [:request/done
               {:status :failure
                :name name
                :error error
                :request-time request-time}])
    (dispatch (conj on-failure error))))

(defn track-request!
  "Kicks off the request tracking process"
  [name request-time]
  (dispatch [:request/start
             {:name name
              :request-time request-time}]))

(defn format-response-kw->fn
  [name]
  (fn [kw]
    (if-not (nil? kw)
      (case kw
        :transit (ajax/transit-response-format)
        :json (ajax/json-response-format {:keywords? true})
        :ring (ajax/ring-response-format)
        :text (ajax/text-response-format)
        :raw (ajax/raw-response-format)
        (js/console.error (str "Response format " (pr-str kw)
                               " is not a vaild format for request \""
                               name "\"")))
      (js/console.warn (str "Missing response format for xhr request \""
                            name "\"")))))

(defn format-request-kw->fn
  [name]
  (fn [kw]
    (when-not (nil? kw)
      (case kw
        :transit (ajax/transit-request-format)
        :json (ajax/json-request-format)
        :url (ajax/url-request-format)
        :text (ajax/text-request-format)
        (js/console.error (str "Request format " (pr-str kw)
                               " is not a vaild format for request \""
                               name "\""))))))

(defn- handle-request
  [{:as request
    :keys [name
           on-success
           on-failure]
    :or {:on-success [:http-no-on-success]
         :on-failure [:http-no-on-failure]}}]
  (let [seq-request-maps (if (sequential? request) request [request])
        request-time (.getTime (js/Date.))]

    (track-request! name request-time)

    (doseq [request seq-request-maps]
      (-> request
          (assoc :on-success (wrap-success! on-success name request-time))
          (assoc :on-failure (wrap-failure! on-failure name request-time))
          (update :response-format (format-response-kw->fn name))
          (update :format (format-request-kw->fn name))
          (dissoc :name)
          request->xhrio-options
          ajax/ajax-request))))

(defn- handle-mock
  [{:as request
    :keys [name
           on-success
           on-failure
           mock]
    :or {:on-success [:request/http-no-on-success]
         :on-failure [:request/http-no-on-failure]}}]
  (let [{:keys [time data error]
         :or {time 200}} mock
        request-time (.getTime (js/Date.))]

    (.setTimeout
     js/window
     (fn []
       (if error
         ((wrap-failure! on-failure name request-time) error)
         ((wrap-success! on-success name request-time) data)))
     time)))

(defn register-events
  [{:keys [start-interceptors
           done-interceptors
           reset-interceptors
           request-interceptors]
    :or {start-interceptors []
         done-interceptors []
         reset-interceptors []
         request-interceptors []}}]

  (reg-event-db
   :request/done
   (into request-interceptors done-interceptors)
   (fn [db [_ {:keys [name request-time error status]}]]
     (assoc-in db [:request name] {:status status
                                   :request-time request-time
                                   :error error})))

  (reg-event-db
   :request/start
   (into request-interceptors start-interceptors)
   (fn [db [_ {:keys [name request-time]}]]
     (assoc-in db [:request name] {:status :loading
                                   :request-time request-time
                                   :error nil})))

  (reg-event-db
   :request/reset
   (into request-interceptors reset-interceptors)
   (fn [db [_ request-name]]
     (assoc-in db [:request request-name] nil)))

  (reg-fx :request handle-request)

  (reg-fx :request-mock handle-mock))
