#!/bin/bash

set -e

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

timestamp=$(date +%s)

pack build nthomsonpivotal/workshop-mm-bot:$timestamp -p $DIR/../../workshop-bot

docker push nthomsonpivotal/workshop-mm-bot:$timestamp