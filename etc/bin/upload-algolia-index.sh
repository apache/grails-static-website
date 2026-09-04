#!/usr/bin/env bash
#
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements. See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership. The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License. You may obtain a copy of the License at
#
#   https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied. See the License for the
# specific language governing permissions and limitations
# under the License.

set -euo pipefail

SCRIPT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
REPOSITORY_ROOT=$(CDPATH= cd -- "$SCRIPT_DIR/../.." && pwd)
cd "$REPOSITORY_ROOT"

if [[ -z "${ALGOLIA_APP_ID:-}" ]]; then
    read -r -p 'Algolia application ID: ' ALGOLIA_APP_ID
    export ALGOLIA_APP_ID
fi

if [[ -z "${ALGOLIA_WRITE_API_KEY:-}" ]]; then
    read -r -s -p 'Algolia write API key: ' ALGOLIA_WRITE_API_KEY
    printf '\n'
    export ALGOLIA_WRITE_API_KEY
fi

if [[ -z "$ALGOLIA_APP_ID" || -z "$ALGOLIA_WRITE_API_KEY" ]]; then
    printf 'ALGOLIA_APP_ID and ALGOLIA_WRITE_API_KEY are required.\n' >&2
    exit 1
fi

export ALGOLIA_COLLECT_EXTERNAL_DOCUMENTATION="${ALGOLIA_COLLECT_EXTERNAL_DOCUMENTATION:-true}"
export GRAILS_WS_URL="${GRAILS_WS_URL:-https://grails.apache.org}"

./gradlew clean uploadAlgoliaIndex \
    --console=plain \
    --no-daemon \
    --no-configuration-cache \
    "$@"
