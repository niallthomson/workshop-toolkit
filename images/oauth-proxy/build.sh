#!/bin/bash

set -e

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

VERSION=$1

if [ -z "$VERSION" ]; then
  VERSION="latest"
fi

pack build nthomsonpivotal/node-oauth-proxy:$VERSION -p $DIR/../../node-oauth-proxy

docker push nthomsonpivotal/node-oauth-proxy:$VERSION