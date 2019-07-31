#!/bin/bash

set -e

export CODER_DIR=/mnt/coder
export CODER_BIN_DIR=$CODER_DIR/bin
export MODULE_DIR=$CODER_DIR/module

export TF_CONFIG_DIR=$MODULE_DIR/terraform
export TF_STATE_DIR=$CODER_DIR/state

export PATH=$PATH:$CODER_BIN_DIR

DESTROY_FILE=$MODULE_DIR/destroy.sh

# Handle Terraform if present
if [ -d "$TF_CONFIG_DIR" ]; then
  mkdir -p $TF_STATE_DIR

  (cd $TF_STATE_DIR && terraform destroy --auto-approve $TF_CONFIG_DIR)
fi

# Run module destroy script if present
if [ -f "$DESTROY_FILE" ]; then
  chmod +x $DESTROY_FILE

  echo "Running $DESTROY_FILE..."

  $DESTROY_FILE
fi