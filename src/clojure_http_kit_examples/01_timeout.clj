(ns clojure-http-kit-examples.01-timeout
  (:require
   [org.httpkit.client :as http]
   [schema.core :as s]))

(s/defn http-get-request
  [url :- s/Str]
  @(http/request {:url url
                  :method :get
                  :timeout 1000
                  :connect-timeout 2000
                  :idle-timeout 60000}))
