#!/bin/bash

set -e

if [ -z "$YML" ]; then
  exit
fi

cat <<EOF | kubectl apply -f -
${YML}
EOF