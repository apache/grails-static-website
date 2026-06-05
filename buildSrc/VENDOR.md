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

# Vendored Sources

This document records third-party sources that have been copied into
`buildSrc/` of `apache/grails-static-website`. The vendoring is required
because the upstream project intentionally does not publish these
modules as Maven artifacts (see "Why vendored, not consumed" below).

The top-level `NOTICE` file at the repository root carries the formal
attribution required by the Apache License 2.0.

---

## Vendored renderer: `grails/doc/*` from apache/grails-core 7.2.x

| Field | Value |
|---|---|
| Upstream repository | https://github.com/apache/grails-core |
| Upstream branch | `7.2.x` |
| Upstream commit (pinned) | `3d2d795680f43e129ce4a736f7e2388ef04166ca` |
| Upstream subproject | `build-logic/docs-core/` |
| Upstream license | Apache License 2.0 |
| Vendored on | 2026-05-01 |

### Why vendored, not consumed

apache/grails-core's `RENAME.md` records that the historical
`org.grails:grails-docs` artifact was deliberately not republished
under the `org.apache.grails` group. A subsequent commit on the 7.2.x
line (`0248afdfcfc do not publish grails-docs-core going forward and
keep internal`) made this explicit upstream policy: the renderer is an
internal build-logic concern of grails-core, not a consumable artifact.

The last published version of the legacy artifact, `grails-docs:6.2.0`,
is incompatible with Gradle 9 because its `PublishGuide` task casts
`Project.ant` to `groovy.util.AntBuilder` (Codehaus Groovy 3 path),
which fails against Gradle 9's `DefaultAntBuilder` extending
`groovy.ant.AntBuilder` (Apache Groovy 4 path).

The 7.x source has been modernized to use `org.gradle.api.AntBuilder`
without a cast and is Gradle 9 + Apache Groovy 4 clean. Vendoring this
modernized source rather than consuming the broken 6.2.0 binary or
pivoting to a different renderer guarantees parity with the legacy
`https://guides.grails.org/` output (same renderer lineage that
produced today's site).

### Vendored files

#### Sources (`buildSrc/src/main/groovy/grails/doc/`)

| Vendored path (relative to `buildSrc/src/main/groovy/`) | Upstream path (relative to grails-core repo root) |
|---|---|
| `grails/doc/DocEngine.groovy` | `build-logic/docs-core/src/main/groovy/grails/doc/DocEngine.groovy` |
| `grails/doc/DocPublisher.groovy` | `build-logic/docs-core/src/main/groovy/grails/doc/DocPublisher.groovy` |
| `grails/doc/asciidoc/AsciiDocEngine.groovy` | `build-logic/docs-core/src/main/groovy/grails/doc/asciidoc/AsciiDocEngine.groovy` |
| `grails/doc/dropdown/CreateReleaseDropDownTask.groovy` | `build-logic/docs-core/src/main/groovy/grails/doc/dropdown/CreateReleaseDropDownTask.groovy` |
| `grails/doc/dropdown/Snapshot.groovy` | `build-logic/docs-core/src/main/groovy/grails/doc/dropdown/Snapshot.groovy` |
| `grails/doc/dropdown/SoftwareVersion.groovy` | `build-logic/docs-core/src/main/groovy/grails/doc/dropdown/SoftwareVersion.groovy` |
| `grails/doc/filters/HeaderFilter.groovy` | `build-logic/docs-core/src/main/groovy/grails/doc/filters/HeaderFilter.groovy` |
| `grails/doc/filters/LinkTestFilter.groovy` | `build-logic/docs-core/src/main/groovy/grails/doc/filters/LinkTestFilter.groovy` |
| `grails/doc/filters/ListFilter.groovy` | `build-logic/docs-core/src/main/groovy/grails/doc/filters/ListFilter.groovy` |
| `grails/doc/gradle/PublishGuideTask.groovy` | `build-logic/docs-core/src/main/groovy/grails/doc/gradle/PublishGuideTask.groovy` |
| `grails/doc/internal/FileResourceChecker.groovy` | `build-logic/docs-core/src/main/groovy/grails/doc/internal/FileResourceChecker.groovy` |
| `grails/doc/internal/StringEscapeCategory.java` | `build-logic/docs-core/src/main/groovy/grails/doc/internal/StringEscapeCategory.java` |
| `grails/doc/internal/UserGuideNode.groovy` | `build-logic/docs-core/src/main/groovy/grails/doc/internal/UserGuideNode.groovy` |
| `grails/doc/internal/YamlTocStrategy.groovy` | `build-logic/docs-core/src/main/groovy/grails/doc/internal/YamlTocStrategy.groovy` |
| `grails/doc/macros/GspTagSourceMacro.groovy` | `build-logic/docs-core/src/main/groovy/grails/doc/macros/GspTagSourceMacro.groovy` |
| `grails/doc/macros/HiddenMacro.groovy` | `build-logic/docs-core/src/main/groovy/grails/doc/macros/HiddenMacro.groovy` |

#### Resources (`buildSrc/src/main/resources/`)

| Vendored path | Upstream path |
|---|---|
| `grails/doc/doc.properties` | `build-logic/docs-core/src/main/resources/grails/doc/doc.properties` |
| `radeox_messages_en.properties` | `build-logic/docs-core/src/main/resources/radeox_messages_en.properties` |

#### Templates (`buildSrc/src/main/template/`)

35 files vendored verbatim:
- `style/*.html` (6 files: `guideItem.html`, `index.html`, `layout.html`, `menu.html`, `referenceItem.html`, `section.html`)
- `css/*.css` (8 files)
- `js/docs.js`
- `fonts/*` (5 files: Font Awesome assets)
- `img/*` (15 files: Grails branding -- to be replaced in a follow-up theme-swap commit)
- `log4j.properties`

All under `build-logic/docs-core/src/main/template/` upstream.

### Deliberately not vendored (out of scope)

| Upstream path | Reason |
|---|---|
| `build-logic/docs-core/src/main/groovy/grails/doc/ant/DocPublisherTask.groovy` | Ant-task variant of `PublishGuideTask`; this project uses the Gradle task. |
| `build-logic/docs-core/src/main/groovy/grails/doc/git/FetchTagsTask.groovy` | Git-tag fetcher for grails-core's own release flow; not relevant to guide rendering. |
| `build-logic/docs-core/src/main/groovy/org/apache/grails/gradle/tasks/bom/*` | BOM extraction tooling for grails-core's published artifacts; not relevant to guide rendering. |
| `build-logic/docs-core/src/test/...` | Upstream's tests for the docs-core module; this project will write its own tests focused on the integration with `conf/guides.yml`. |

### Modifications applied

None. All vendored files retain their upstream contents byte-for-byte,
including the original Apache 2.0 license headers. The
`apache/grails-core` source tree was extracted via `git archive` against
the pinned commit and copied into place via `robocopy`, with the four
out-of-scope subdirectories filtered out.

If future patches are needed, they MUST be recorded here with rationale,
the upstream issue link (if any), and a target date for upstreaming the
fix back to grails-core.

### Re-vendor procedure

When upstream `apache/grails-core` 7.x evolves the renderer in a way we
need to track:

```pwsh
$grailsCoreRepo = 'C:\path\to\apache\grails-core'
$thisRepo      = 'C:\path\to\apache\grails-static-website'
$pin           = '<new-commit-sha>'

# 1. Verify upstream is on the desired commit
git -C $grailsCoreRepo fetch origin
git -C $grailsCoreRepo log -1 --format='%h %s %ci' $pin

# 2. Archive + extract upstream subtree to a temp dir
$temp = Join-Path $env:TEMP "phase8-revendor-$(Get-Date -Format yyyyMMdd-HHmmss)"
New-Item -ItemType Directory -Path $temp -Force | Out-Null
Push-Location $grailsCoreRepo
git archive --format=tar $pin -- `
    build-logic/docs-core/src/main/groovy `
    build-logic/docs-core/src/main/resources `
    build-logic/docs-core/src/main/template `
    | tar -x -C $temp
Pop-Location

# 3. Stage the replacement subset (exclude ant/, git/, BOM tooling)
$srcRoot     = "$temp\build-logic\docs-core\src\main"
$destRoot    = "$thisRepo\buildSrc\src\main"
robocopy "$srcRoot\groovy\grails\doc" "$destRoot\groovy\grails\doc" /S /E /XD ant git /MIR
robocopy "$srcRoot\resources"         "$destRoot\resources"         /S /E /MIR
robocopy "$srcRoot\template"          "$destRoot\template"          /S /E /MIR

# 4. Update this file and the root NOTICE with the new commit SHA + date
# 5. Run ./gradlew :buildSrc:compileGroovy to verify
# 6. Run :buildAllGuides + the renderer parity test before opening a PR
```

### Audit log

| Date | Upstream commit | Action | Notes |
|---|---|---|---|
| 2026-05-01 | `3d2d795680f43e129ce4a736f7e2388ef04166ca` | Initial vendor | apache/grails-core 7.2.x HEAD; renderer subtree (`grails/doc/...`) verified identical across 7.0.x / 7.1.x / 7.2.x as of this date. |
