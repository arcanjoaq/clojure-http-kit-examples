(ns clojure-http-kit-examples.03-trust-store-test
  (:require
   [clj-test-containers.core :as tc]
   [clojure-http-kit-examples.03-trust-store :refer [https-get-request]]
   [clojure-http-kit-examples.certs :as certs]
   [clojure-http-kit-examples.keystores :as keystores]
   [schema.core :as s]
   [state-flow.api :as flow :refer [defflow flow match?]]))

(System/setProperty "javax.net.debug" "ssl,handshake,verbose")

(s/defn ^:private start!
  []
  (-> (tc/create {:image-name    "nginx:latest"
                  :exposed-ports [80 443]
                  :wait-for {:http {:path "/health"
                                    :port 80
                                    :status 200}}})
      (tc/bind-filesystem! {:host-path "resources/nginx.conf"
                            :container-path "/etc/nginx/nginx.conf"
                            :mode :read-only})

      (tc/bind-filesystem! {:host-path "/tmp/key.pem"
                            :container-path "/tmp/key.pem"
                            :mode :read-only})

      (tc/bind-filesystem! {:host-path "/tmp/certificate.pem"
                            :container-path "/tmp/certificate.pem"
                            :mode :read-only})
      (tc/start!)))

(defn- create-certs!
  []
  (let [root (certs/self-signed-root-ca "C=BR, ST=Sao Paulo, L=Sao Paulo, O=My Root CA, OU=IT, CN=api.*" 365)
        cert (certs/signed-cert root "C=BR, ST=Sao Paulo, L=Sao Paulo, O=My Org, OU=IT, CN=api.localhost" 825)]
    (certs/save-key-to-der (:keypair root) "/tmp/root-ca-key.der")
    (certs/save-cert-to-der (:certificate root) "/tmp/root-ca.der")
    (certs/save-key-to-pem (:keypair root) "/tmp/root-ca-key.pem")
    (certs/save-cert-to-pem (:certificate root) "/tmp/root-ca.pem")
    (certs/save-key-to-der (:keypair cert) "/tmp/key.der")
    (certs/save-cert-to-der (:certificate cert) "/tmp/certificate.der")
    (certs/save-key-to-pem (:keypair cert) "/tmp/key.pem")
    (certs/save-cert-to-pem (:certificate cert) "/tmp/certificate.pem")))

(defflow http-out-test
  (flow "Creating certificates"
        (flow/invoke create-certs!)

        (flow "Given a running server"
              [:let [container (start!)]]

              (flow "When I execute an http get to /health on port 443 with a pem rootCA"
                    ;; add 127.0.0.1 api.localhost in your /etc/hosts
                    [:let [base-url (str "https://api.localhost:" (get (:mapped-ports container) 443))
                           res (https-get-request (str base-url "/health")
                                                  (keystores/->keystore "/tmp/root-ca.pem"))]]
                    (flow "Then the response will be 200"
                          (match? {:status 200} res)))

              (flow "When I execute an http get to /health on port 443 with a der rootCA"
                    ;; add 127.0.0.1 api.localhost in your /etc/hosts
                    [:let [base-url (str "https://api.localhost:" (get (:mapped-ports container) 443))
                           res (https-get-request (str base-url "/health")
                                                  (keystores/->keystore "/tmp/root-ca.der"))]]
                    (flow "Then the response will be 200"
                          (match? {:status 200} res)))

              (flow/invoke #(tc/stop! container)))))

(comment
  (-> "https://api.localhost/health"
      (https-get-request
       (keystores/->keystore "/tmp/root-ca.pem"))
      :body)

  (-> "https://api.localhost/health"
      (https-get-request
       (keystores/->keystore "/tmp/root-ca.der"))
      :body))
