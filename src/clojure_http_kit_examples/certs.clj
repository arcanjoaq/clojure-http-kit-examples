(ns clojure-http-kit-examples.certs
  (:require
   [schema.core :as s])
  (:import
   [java.io FileOutputStream FileWriter]
   [java.math BigInteger]
   [java.security KeyPair PublicKey Security]
   [java.security KeyPairGenerator SecureRandom]
   [java.util Calendar Date]
   [javax.naming.ldap LdapName Rdn]
   [javax.security.auth.x500 X500Principal]
   [javax.security.cert X509Certificate]
   [org.bouncycastle.asn1.x500 X500Name]
   [org.bouncycastle.asn1.x509
    BasicConstraints
    ExtendedKeyUsage
    Extension
    GeneralName
    GeneralNames
    KeyPurposeId
    KeyUsage]
   [org.bouncycastle.cert.jcajce JcaX509CertificateConverter JcaX509v3CertificateBuilder]
   [org.bouncycastle.cert.jcajce JcaX509ExtensionUtils]
   [org.bouncycastle.jce.provider BouncyCastleProvider]
   [org.bouncycastle.openssl.jcajce JcaPEMWriter]
   [org.bouncycastle.operator.jcajce JcaContentSignerBuilder]))

(Security/addProvider (BouncyCastleProvider.))

(s/defn ^:private gen-keypair :- KeyPair
  [bits :- s/Int]
  (let [gen (KeyPairGenerator/getInstance "RSA")]
    (.initialize gen bits (SecureRandom.))
    (.generateKeyPair gen)))

(s/defn ^:private days-from-now :- Date
  [n :- s/Int]
  (let [cal (Calendar/getInstance)]
    (.add cal Calendar/DAY_OF_YEAR n)
    (.getTime cal)))

(s/defn self-signed-root-ca :- {s/Keyword s/Any}
  [subject :- s/Str
   days    :- s/Int]
  (let [kp (gen-keypair 4096)
        now (Date.)
        until (days-from-now days)
        subj (X500Name. subject)
        builder (JcaX509v3CertificateBuilder.
                 ^X500Name subj
                 ^BigInteger (BigInteger/valueOf (System/currentTimeMillis))
                 ^Date now
                 ^Date until
                 ^X500Name subj
                 ^PublicKey (.getPublic kp))
        _ (.addExtension builder Extension/basicConstraints true (BasicConstraints. true))
        signer (.build (JcaContentSignerBuilder. "SHA256withRSA") (.getPrivate kp))
        ^X509Certificate cert (.getCertificate (JcaX509CertificateConverter.) (.build builder signer))]
    {:keypair kp :certificate cert}))

(s/defn get-rdn-value :- s/Str
  [rdn-type           :- s/Str
   principal          :- X500Principal]
  (let [ldap-name (LdapName. (.getName principal))]
    (some (fn [^Rdn rdn]
            (when (= rdn-type (.getType rdn))
              (.getValue rdn)))
          (.getRdns ldap-name))))

(s/defn signed-cert :- {s/Keyword s/Any}
  [root-ca :- {s/Keyword s/Any}
   subject :- s/Str
   days    :- s/Int]
  (let [kp (gen-keypair 2048)
        now (Date.)
        until (days-from-now days)
        subj (X500Principal. subject)
        domain-name (get-rdn-value "CN" subj)
        builder (JcaX509v3CertificateBuilder.
                 ^X500Principal (.getSubjectX500Principal (:certificate root-ca))
                 ^BigInteger (BigInteger/valueOf (System/currentTimeMillis))
                 ^Date now
                 ^Date until
                 ^X500Principal subj
                 ^PublicKey (.getPublic kp))
        ext-utils (JcaX509ExtensionUtils.)
        _ (.addExtension builder Extension/subjectKeyIdentifier false
                         (.createSubjectKeyIdentifier ext-utils (.getPublic kp)))
        _ (.addExtension builder Extension/authorityKeyIdentifier false
                         (.createAuthorityKeyIdentifier ext-utils (.getPublic (:keypair root-ca))))
        _ (.addExtension builder Extension/keyUsage true
                         (KeyUsage. (bit-or KeyUsage/digitalSignature KeyUsage/keyEncipherment)))
        _ (.addExtension builder Extension/extendedKeyUsage false
                         (ExtendedKeyUsage. (into-array KeyPurposeId [KeyPurposeId/id_kp_serverAuth])))
        _ (.addExtension builder Extension/subjectAlternativeName false
                         (GeneralNames. (into-array GeneralName [(GeneralName. GeneralName/dNSName domain-name)])))
        signer (.build (JcaContentSignerBuilder. "SHA256withRSA") (.getPrivate (:keypair root-ca)))
        ^X509Certificate cert (.getCertificate (JcaX509CertificateConverter.) (.build builder signer))]
    {:keypair kp :certificate cert}))

(s/defn save-key-to-der
  [kp   :- KeyPair
   path :- s/Str]
  (with-open [fos (FileOutputStream. path)]
    (.write fos (.getEncoded (.getPrivate kp)))))

(s/defn save-cert-to-der
  [^java.security.cert.X509Certificate cert
   path :- s/Str]
  (with-open [fos (FileOutputStream. path)]
    (.write fos (.getEncoded cert))))

(s/defn save-key-to-pem
  [kp   :- KeyPair
   path :- s/Str]
  (with-open [writer (JcaPEMWriter. (FileWriter. path))]
    (.writeObject writer (.getPrivate kp))))

(s/defn save-cert-to-pem
  [cert
   path :- s/Str]
  (with-open [writer (JcaPEMWriter. (FileWriter. path))]
    (.writeObject writer cert)))
