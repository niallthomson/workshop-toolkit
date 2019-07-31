#!/bin/bash

ADMIN_PASSWORD="$1"

ADMIN_USERNAME=admin
TEAM_NAME=workshop

POD_NAME=$(kubectl -n mattermost get pods --selector='app.kubernetes.io/name=mattermost-team-edition' -o json | jq -r '.items[0].metadata.name')

# Create admin user and add to the workshop team
kubectl -n mattermost exec -it $POD_NAME -- bin/mattermost user create --email "$ADMIN_USERNAME@localhost" --username $ADMIN_USERNAME --password "$ADMIN_PASSWORD" --system_admin

kubectl -n mattermost exec -it $POD_NAME -- bin/mattermost team create --name $TEAM_NAME --display_name Workshop

kubectl -n mattermost exec -it $POD_NAME -- bin/mattermost team add $TEAM_NAME $ADMIN_USERNAME