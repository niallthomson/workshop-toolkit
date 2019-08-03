#!/bin/bash

NAMESPACE=$1
DNS_SUFFIX=$2
ACME_DNS_PROVIDER=$3

cat <<EOF | kubectl apply -f -
apiVersion: certmanager.k8s.io/v1alpha1
kind: Certificate
metadata:
  name: shared-cert
  namespace: ${NAMESPACE}
spec:
  acme:
    config:
    - dns01:
        provider: ${ACME_DNS_PROVIDER}
      domains:
      - '*.${DNS_SUFFIX}'
  dnsNames:
  - '*.${DNS_SUFFIX}'
  issuerRef:
    kind: ClusterIssuer
    name: letsencrypt-prod
  secretName: wildcard-tls-secret
EOF