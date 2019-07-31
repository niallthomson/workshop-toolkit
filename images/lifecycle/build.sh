#!/bin/bash

set -e

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

docker build -t nthomsonpivotal/code-server-lifecycle $DIR

docker push nthomsonpivotal/code-server-lifecycle