#!/usr/bin/env bash

cleanup() {
  rm /tmp/keystore.jks \
    /tmp/root-ca-key.der /tmp/root-ca-key.pem /tmp/root-ca.der /tmp/root-ca.srl \
    /tmp/key.pem /tmp/certificate.pem /tmp/key.der /tmp/certificate.der /tmp/certificate.csr /tmp/certificate.ext \
    /tmp/client.key /tmp/client.pem /tmp/client.csr /tmp/client.der \
    >/dev/null 2>&1
}

root_ca() {
  openssl genrsa -out /tmp/root-ca.key 4096
  openssl req -x509 -new -nodes -key /tmp/root-ca.key -sha256 -days 3650 \
    -subj "/C=BR/ST=Sao Paulo/L=Sao Paulo/O=My Root CA/OU=IT/CN=api.*" \
    -out /tmp/root-ca.pem
}

server_cert() {
  openssl genrsa -out /tmp/key.pem 2048

  openssl req -new -key /tmp/key.pem \
    -subj "/C=BR/ST=Sao Paulo/L=Sao Paulo/O=My Org/OU=IT/CN=api.localhost" \
    -out /tmp/certificate.csr

  cat >/tmp/certificate.ext <<EOF
authorityKeyIdentifier=keyid,issuer
basicConstraints=CA:FALSE
keyUsage = digitalSignature, keyEncipherment
extendedKeyUsage = serverAuth
subjectAltName = @alt_names

[alt_names]
DNS.1 = api.localhost
EOF

  openssl x509 -req -in /tmp/certificate.csr -CA /tmp/root-ca.pem -CAkey /tmp/root-ca.key -CAcreateserial \
    -out /tmp/certificate.pem -days 825 -sha256 -extfile /tmp/certificate.ext
}

client_cert() {
  openssl genpkey -algorithm RSA -out /tmp/client.key
  openssl req -new -key /tmp/client.key -out /tmp/client.csr -subj "/C=BR/ST=Sao Paulo/L=Sao Paulo/O=My Org/OU=IT/CN=mTLS"
  openssl x509 -req -in /tmp/client.csr -CA /tmp/root-ca.pem -CAkey /tmp/root-ca.key -out /tmp/client.pem -CAcreateserial
}

convert_certs() {
  openssl x509 -outform der -inform pem -in /tmp/client.pem -out /tmp/client.der
  openssl x509 -outform der -inform pem -in /tmp/certificate.pem -out /tmp/certificate.der
  openssl x509 -outform der -inform pem -in /tmp/root-ca.pem -out /tmp/root-ca.der
  openssl pkcs12 -export -out /tmp/keystore.jks -inkey /tmp/client.key -in /tmp/client.pem -passout pass:changeit
}

cleanup
root_ca
server_cert
client_cert
convert_certs
