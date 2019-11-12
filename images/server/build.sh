#!/bin/bash

set -e

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

timestamp=$(date +%s)

docker build -t nthomsonpivotal/code-server-workspace:$timestamp $DIR

docker push nthomsonpivotal/code-server-workspace:$timestamp