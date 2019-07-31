#!/bin/bash

set -e

export CODER_DIR=/mnt/coder
export CODER_BIN_DIR=$CODER_DIR/bin
export MODULE_DIR=$CODER_DIR/module

export TF_CONFIG_DIR=$MODULE_DIR/terraform
export TF_STATE_DIR=$CODER_DIR/state

BOOTSTRAP_FILE=$MODULE_DIR/bootstrap.sh

sudo chown coder:coder -R $CODER_DIR

mkdir -p $CODER_BIN_DIR /mnt/coder/bashrc.d

# Create a noop file to prevent bash errors on terminal start
touch /mnt/coder/bashrc.d/noop.bashrc
chmod +x /mnt/coder/bashrc.d/noop.bashrc

rm -rf $MODULE_DIR

if [ -z "$WORKSHOP_GIT" ]; then
  echo "No module git specified"
  mkdir $MODULE_DIR
else
  git clone $WORKSHOP_GIT $MODULE_DIR
fi

# Handle Terraform if present
if [ -d "$TF_CONFIG_DIR" ]; then
  mkdir -p $TF_STATE_DIR

  (cd $TF_STATE_DIR && terraform init $TF_CONFIG_DIR)

  (cd $TF_STATE_DIR && terraform apply --auto-approve $TF_CONFIG_DIR)
fi

# Run module bootstrap script if present
if [ -f "$BOOTSTRAP_FILE" ]; then
  chmod +x $BOOTSTRAP_FILE

  echo "Running $BOOTSTRAP_FILE..."

  $BOOTSTRAP_FILE
fi