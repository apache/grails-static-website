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

# Vendored theme + common docs - license audit

Theme assets and shared AsciiDoc snippets vendored from `grails-guides/grails-guides-template`.

## Source pin

| Field | Value |
|---|---|
| Repo            | https://github.com/grails-guides/grails-guides-template |
| Branch          | `6.0.x` |
| Commit SHA      | `901cffb0c098027b38adeb0f3e62aa1a2012c6c5` |
| Capture date    | 2026-04-30 |

## What is vendored

| Destination (here) | Source (grails-guides-template) | Files |
|---|---|---|
| `../common/`              | `src/main/docs/`              | 46 `.adoc` snippets included by guides via `:commondir:` |
| `./css/`                  | `src/main/resources/css/`     | 5 stylesheets (theme + FontAwesome) |
| `./fonts/`                | `src/main/resources/fonts/`   | 42 font files (Archia, Roboto, FontAwesome) |
| `./img/`                  | `src/main/resources/img/`     | 16 theme images (logos, icons, screenshots) |
| `./js/`                   | `src/main/resources/js/`      | 3 theme JavaScript files |
| `./style/`                | `src/main/resources/style/`   | 2 HTML templates consumed by PublishGuide |

Total: 114 files, ~3.04 MiB. Bit-for-bit identical to the source pin.

The macOS-only `*.napkin` design-source bundles in `src/main/resources/img/` were NOT vendored. The rendered `.png` siblings already cover the runtime need.

## Apache License compliance

The source repository is `grails-guides/grails-guides-template`, published under the Apache License 2.0 (see its `LICENSE` file). Vendored content inherits that license.

ASF license headers are NOT yet added to the vendored `.adoc` files in `../common/`. Headers are added in a separate follow-up commit so any byte-equality regression test against the upstream source can run cleanly first.

## Third-party fonts

| Font family | License | Notes |
|---|---|---|
| Archia (light/regular/medium/semibold/bold/thin)   | TBD - confirm distribution rights with the Grails Foundation. **Open item.** If Archia is not redistributable under Apache 2.0, swap to an alternative (Inter, Source Sans Pro, or system fonts). |
| Roboto (light/medium/regular)                       | Apache License 2.0 (Google Fonts). Safe to redistribute. |
| FontAwesome 4.x free (`.eot/.svg/.ttf/.woff/.woff2/.otf`) | SIL OFL 1.1 (icons) + MIT (code). Safe to redistribute. |

A `NOTICE` file naming each third-party font and its license will land alongside the ASF license headers in the follow-up commit referenced above.
