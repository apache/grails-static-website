<!--
SPDX-License-Identifier: Apache-2.0

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    https://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
-->

# scripts/

Operational scripts. Each script documents its own arguments and
preconditions in a header comment.

## Tooling prerequisites

Local development assumes the following are on `PATH`:

| Tool | Purpose |
|---|---|
| `gh` (GitHub CLI), authenticated | API calls, PR creation, branch settings |
| `yq` (Mike Farah Go version) | YAML parsing in shell scripts |
| JDK 17+ | Gradle build (JDK 18+ also enables `jwebserver` for local preview, JEP 408) |
| `rsync` (Ubuntu CI default; `brew install rsync` on macOS) | `publish.sh` deploy step |
| `git` | repo operations |

## Running a script

Scripts are POSIX shell unless otherwise noted:

```bash
./scripts/<name>.sh [args]
```
