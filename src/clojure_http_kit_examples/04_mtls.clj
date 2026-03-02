(ns clojure-http-kit-examples.04-mtls
  (:require
   [org.httpkit.client :as http]
   [schema.core :as s])
  (:import
   [java.security KeyStore]
   [javax.net.ssl
    KeyManagerFactory
    SSLContext
    SSLEngine
    TrustManagerFactory]))

(s/defn ->ssl-engine :- SSLEngine
  [key-store          :- KeyStore
   key-store-password :- s/Str
   trust-store        :- KeyStore]
  (let [kmf (KeyManagerFactory/getInstance
             (KeyManagerFactory/getDefaultAlgorithm))

        _ (.init kmf key-store (char-array key-store-password))

        tmf (TrustManagerFactory/getInstance
             (TrustManagerFactory/getDefaultAlgorithm))

        _ (.init tmf trust-store)]

    (.createSSLEngine (doto (SSLContext/getInstance "TLS")
                        (.init (.getKeyManagers kmf) (.getTrustManagers tmf) nil)))))

(s/defn https-get-request
  [url                :- s/Str
   key-store          :- KeyStore
   key-store-password :- s/Str
   trust-store        :- KeyStore]
  @(http/request {:url url
                  :method :get
                  :sslengine (->ssl-engine key-store key-store-password trust-store)
                  :timeout 1000
                  :connect-timeout 60000
                  :idle-timeout 60000
                  :as :text
                  :insecure? false}))
