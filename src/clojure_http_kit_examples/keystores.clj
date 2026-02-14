(ns clojure-http-kit-examples.keystores
  (:require
   [schema.core :as s])
  (:import
   [java.io FileInputStream]
   [java.security KeyStore]
   [java.security.cert CertificateFactory]
   [javax.security.cert X509Certificate]))

(s/defn ->keystore :- KeyStore
  [pem-or-der-path :- s/Str]
  (let [cf (CertificateFactory/getInstance "X.509")
        ks (KeyStore/getInstance (KeyStore/getDefaultType))]
    (.load ks nil nil)
    (with-open [fis (FileInputStream. pem-or-der-path)]
      (let [cert ^X509Certificate (.generateCertificate cf fis)]
        (.setCertificateEntry ks "cert" cert)))
    ks))
