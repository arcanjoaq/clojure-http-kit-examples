(ns clojure-http-kit-examples.03-trust-store
  (:require
   [org.httpkit.client :as http]
   [schema.core :as s])
  (:import
   [java.security KeyStore]
   [javax.net.ssl SSLContext SSLEngine TrustManagerFactory]))

(s/defn trust-store->ssl-engine :- SSLEngine
  [trust-store :- KeyStore]
  (let [tmf (TrustManagerFactory/getInstance
             (TrustManagerFactory/getDefaultAlgorithm))]
    (.init tmf trust-store)
    (.createSSLEngine (doto (SSLContext/getInstance "TLS")
                        (.init nil (.getTrustManagers tmf) nil)))))

(s/defn https-get-request
  [url         :- s/Str
   trust-store :- KeyStore]
  @(http/request {:url url
                  :method :get
                  :sslengine (trust-store->ssl-engine trust-store)
                  :timeout 1000
                  :connect-timeout 60000
                  :idle-timeout 60000
                  :as :text
                  :insecure? false}))
