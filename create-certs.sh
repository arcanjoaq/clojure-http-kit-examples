#!/usr/bin/env bash
root_ca() {
  openssl genrsa -out /tmp/root-ca.key 4096
  openssl req -x509 -new -nodes -key /tmp/root-ca.key -sha256 -days 3650 \
    -subj "/C=BR/ST=Sao Paulo/L=Sao Paulo/O=My Root CA/OU=IT/CN=api.*" \
    -out /tmp/root-ca.pem
}

cert_pair() {
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

convert_certs() {
  openssl x509 -outform der -inform pem -in /tmp/certificate.pem -out /tmp/certificate.der
  openssl x509 -outform der -inform pem -in /tmp/root-ca.pem -out /tmp/root-ca.der
}

root_ca
cert_pair
convert_certs
