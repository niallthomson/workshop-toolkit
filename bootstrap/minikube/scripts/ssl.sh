#!/bin/bash

set -e

CERT_PATH=$1
KEY_PATH=$2

kubectl -n mattermost create secret tls mattermost-tls-secret --cert=$CERT_PATH --key=$KEY_PATH
kubectl -n mattermost create secret tls oauth-proxy-tls-secret --cert=$CERT_PATH --key=$KEY_PATH
kubectl -n coder create secret tls coder-tls-secret --cert=$CERT_PATH --key=$KEY_PATH