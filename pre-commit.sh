#!/usr/bin/env bash

function main() {
  lein "do" clean, ancient upgrade, lint-fix, test, cloverage || exit 1
}

function setup() {
  if [ -n "${DEBUG}" ] && [ "${DEBUG}" == "true" ]; then
    set -x
  fi
}

setup
main
