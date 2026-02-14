(ns clojure-http-kit-examples.01-http-out-test
  (:require
   [clj-test-containers.core :as tc]
   [clojure-http-kit-examples.01-timeout :refer [http-get-request]]
   [state-flow.api :as flow :refer [defflow flow match?]]))

(defflow http-out-test
  (flow "Given a running server"
        [:let [container (-> (tc/create {:image-name    "mockserver/mockserver"
                                         :exposed-ports [1080]
                                         :wait-for      {:wait-strategy   :http
                                                         :path            "/health"
                                                         :port            1080
                                                         :method          "GET"
                                                         :status-codes    [200]
                                                         :tls             false
                                                         :read-timout     5
                                                         :headers         {"Accept" "text/plain"}
                                                         :startup-timeout 20}
                                         :env-vars      {"MOCKSERVER_INITIALIZATION_JSON_PATH" "/config/expectations.json"}})
                             (tc/bind-filesystem! {:host-path      "resources"
                                                   :container-path "/config"
                                                   :mode           :read-only})
                             (tc/start!))
               base-url (str "http://" (:host container) ":" (get (:mapped-ports container) 1080))]]

        (flow "When I execute an http get to /health"
              [:let [res (http-get-request (str base-url "/health"))]]

              (flow "Then the response will be 200"
                    (match? {:status 200} res)))

        (flow "When I execute an http get to /address"
              [:let [res (http-get-request (str base-url "/address"))]]

              (flow "Then the response will be 200"
                    (match? {:status 200
                             :headers {:content-type "application/json"}
                             :body "{ \"street\" : \"42, Lower Tower St\", \"city\": \"Birmingham\" }"} res)))

        (flow/invoke #(tc/stop! container))))
