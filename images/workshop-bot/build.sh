#!/bin/bash

set -e

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

VERSION=$1

if [ -z "$VERSION" ]; then
  VERSION="latest"
fi

pack build nthomsonpivotal/workshop-mm-bot:$VERSION -p $DIR/../../workshop-bot

docker push nthomsonpivotal/workshop-mm-bot:$VERSION