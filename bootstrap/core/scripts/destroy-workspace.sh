#!/bin/bash

set -e

kubectl -n ${NAMESPACE} delete deployment coder-deployment-${ID} --ignore-not-found=true

cat <<EOF | kubectl apply -f -
apiVersion: batch/v1
kind: Job
metadata:
  name: coder-destroy-${ID}
  namespace: ${NAMESPACE}
spec:
  template:
    metadata:
      name: coder-cleanup-${ID}
    spec:
      containers:
      - name: destroy
        image: nthomsonpivotal/code-init
        imagePullPolicy: Always
        command:
         - "/src/init-scripts/destroy.sh"
        env:
        - name: WORKSHOP_ID
          value: "${ID}"
        volumeMounts:
        - name: coder-storage-${ID}
          mountPath: /mnt/coder
        envFrom:
        - secretRef:
            name: workspaces-secrets
      volumes:
      - name: coder-storage-${ID}
        persistentVolumeClaim:
          claimName: coder-volume-${ID}
      restartPolicy: Never
EOF

kubectl -n ${NAMESPACE} wait --for=condition=complete --timeout=120s job/coder-destroy-${ID}

kubectl -n ${NAMESPACE} delete job coder-destroy-${ID} --ignore-not-found=true

# Necessary to workaround TF not interpolating correct YML templates
kubectl -n ${NAMESPACE} delete ingress coder-ingress-${ID} coder-ingress-app-${ID} --ignore-not-found=true
kubectl -n ${NAMESPACE} delete svc coder-svc-${ID} --ignore-not-found=true
kubectl -n ${NAMESPACE} delete pvc coder-volume-${ID} --ignore-not-found=true