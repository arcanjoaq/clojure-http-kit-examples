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

(s/defn pkcs12->keystore :- KeyStore
  [pkcs12-path :- s/Str
   password    :- s/Str]
  (let [keystore (KeyStore/getInstance "PKCS12")
        pass-chars (when password
                     (char-array password))]
    (with-open [fis (FileInputStream. pkcs12-path)]
      (.load keystore fis pass-chars))
    keystore))
