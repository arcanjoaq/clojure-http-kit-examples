#!/usr/bin/env bash
SCRIPT=$(readlink -f "$0")
SCRIPT_PATH=$(dirname "${SCRIPT}")

create_certs() {
  "${SCRIPT_PATH}/create-certs.sh" || exit 1
}

nginx() {
  docker run --rm \
    --name nginx \
    -p 80:80 \
    -p 443:443 \
    -v "${SCRIPT_PATH}/resources/nginx_mtls.conf:/etc/nginx/nginx.conf:ro" \
    -v /tmp/key.pem:/tmp/key.pem:ro \
    -v /tmp/certificate.pem:/tmp/certificate.pem:ro \
    -v /tmp/root-ca.pem:/tmp/root-ca.pem \
    nginx:latest
}

execute_request() {
  curl --cacert /tmp/root-ca.pem --cert /tmp/client.pem --key /tmp/client.key https://api.localhost/health
}

create_certs
nginx
