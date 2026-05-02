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

# Grails Website

[![Build Status](https://github.com/apache/grails-static-website/workflows/Publish/badge.svg)](https://github.com/apache/grails-static-website/actions)

Source for the Apache Grails website at [https://grails.apache.org/](https://grails.apache.org/) and the guides site at [https://grails.apache.org/guides/](https://grails.apache.org/guides/). Built as a static site with [Gradle](https://gradle.org); the build logic lives in [`buildSrc/`](buildSrc/).

The cron-driven [`publish.yml`](.github/workflows/publish.yml) workflow builds this repository and pushes the result to the [`asf-site-production` branch on `apache/grails-website`](https://github.com/apache/grails-website/tree/asf-site-production), which Apache Infra mirrors to `grails.apache.org`.

The legacy `https://guides.grails.org/` host is kept alive serving meta-refresh redirects to the new canonical URLs.

## Repository layout

| Path | What lives here |
|---|---|
| `pages/` | Hand-authored HTML pages on the main site (community, download, FAQ, etc.) |
| `posts/` | Blog posts in Markdown |
| `assets/` | CSS, JS, fonts, images for the main site |
| `templates/` | HTML chrome shared across the main site and guide pages |
| `conf/` | Site configuration: `guides.yml` (guide registry), `releases.yml` (Grails versions), `csp-allowlist.yml`, `legacy-guide-paths.txt` (redirect manifest input) |
| `guides/` | Vendored guide content - one subdirectory per guide, then per Grails major version (see [Authoring a Guide](#authoring-a-guide)) |
| `buildSrc/` | Gradle plugin (`website.gradle.GrailsWebsitePlugin`) and all custom tasks |
| `scripts/` | Reserved for migration helpers; see [Appendix G of the migration plan](https://github.com/apache/grails-static-website/issues/354) - everything operational is a Gradle task |

## Building locally

Tasks use the `grails website` group:

```bash
./gradlew tasks --group="grails website"
```

### Main site

[https://grails.apache.org](https://grails.apache.org)

```bash
./gradlew build --console=plain
```

Output lands in `build/dist/`.

### Guides site

[https://grails.apache.org/guides/](https://grails.apache.org/guides/)

```bash
./gradlew buildGuides --console=plain
```

This is the aggregate task. To render a single guide-version while iterating on its source:

```bash
./gradlew renderGuide_<name>_<version> --console=plain
# e.g. ./gradlew renderGuide_creating-your-first-grails-app_6
```

To check `conf/guides.yml` against the schema:

```bash
./gradlew validateGuides -PvalidationMode=both
```

To run the full guide verification harness (warning gate, link crawl, structural diff, CSP scan, acceptance report):

```bash
./gradlew verifyAllGuides
```

### Local link rewriting

The build emits absolute URLs to `https://grails.apache.org/`. For local preview, point links at your local webserver:

```bash
export GRAILS_WS_URL=http://127.0.0.1:8000
```

## Running the website locally

Generate the site, then serve `build/dist/` with any static-file server.

### Using jwebserver (JDK 19+)

JDK 19+ ships with a built-in static-file server called [`jwebserver`](https://docs.oracle.com/en/java/javase/25/docs/specs/man/jwebserver.html):

```bash
$ jwebserver -d "$(pwd)/build/dist"
Binding to loopback by default. For all interfaces use "-b 0.0.0.0" or "-b ::".
Serving /home/user/grails-static-website/build/dist and subdirectories on 127.0.0.1 port 8000
URL http://127.0.0.1:8000/
```

### Using Python

```bash
python3 -m http.server 8080 --directory build/dist
```

### Using npm + live-server

For live auto-reload, install [`live-server`](https://www.npmjs.com/package/live-server):

```bash
npm install -g live-server
live-server build/dist
```

Combine with continuous Gradle builds in a separate shell for full reloading:

```bash
./gradlew build --continuous
```

## Authoring a Guide

A guide on `https://grails.apache.org/guides/<name>/<version>/guide/index.html` is the rendered form of an AsciiDoc source tree under [`guides/<name>/v<N>/`](guides/) plus a registry entry in [`conf/guides.yml`](conf/guides.yml).

### The three tiers

Pick the tier that matches the kind of guide you want to ship. The PR you open against this repository is the same shape in all three tiers; the difference is whether the guide also needs an external sample-app repository on the [`grails-guides` org](https://github.com/grails-guides) for code that readers can clone and run.

| Tier | Description | External `grails-guides/<name>` repo? |
|---|---|---|
| 1 - **Documentation only** | Pure narrative. No `initial/` or `complete/` code listings, no `include::{sourceDir}/...` directives in the guide body. | No |
| 2 - **Initial code** | Narrative plus a starting-point sample (an empty Grails skeleton or a partial app readers fill in as they follow along). The repository contains an `initial/` directory; the guide may include snippets from it. | Yes |
| 3 - **Initial and complete code** | Narrative plus both a starting-point sample (`initial/`) and a finished reference (`complete/`). | Yes |

If your guide needs a sample repository (Tiers 2 and 3), open it via the PMC **before** you open the PR against this repository. See [Requesting a sample-app repository](#requesting-a-sample-app-repository).

### Source layout per guide

Every guide-version lives at `guides/<name>/v<N>/`:

```
guides/<name>/v<N>/
├── guide/
│   ├── index.adoc        # AsciiDoc entry point - all chapter includes start here
│   └── <chapter>.adoc    # one file per chapter
├── snippets/             # vendored copies of code that include:: directives reference
├── manifest.yml          # per-version metadata consumed by the renderer
└── toc.yml               # table-of-contents structure
```

`manifest.yml` looks like:

```yaml
title: 'My Guide Title'
subtitle: 'A short subtitle'
authors: ['Your Name']
category: 'Some Category'
publicationDate: '2026-05-02'
githubSlug: 'grails-guides/<name>'   # only present for Tiers 2 and 3
githubBranch: 'grails6'              # only present for Tiers 2 and 3
```

`toc.yml` lists chapters in the order they should appear (matching the `include::` order in `guide/index.adoc`).

### Registering the guide in `conf/guides.yml`

Add an entry to the top-level `guides:` list. **Tier 1** (no sample repo) omits the `sampleRef` block:

```yaml
- name: 'my-guide-name'
  title: 'My Guide Title'
  subtitle: 'A short subtitle'
  authors: ['Your Name']
  category: 'Some Category'
  publicationDate: '2026-05-02'
  versions:
    '6':
      sourcePath: guides/my-guide-name/v6
      publicationDate: '2026-05-02'
      tags: ['grails6', 'topic']
```

**Tiers 2 and 3** add a `sampleRef` block per version pinning the exact upstream commit you vendored snippets from:

```yaml
- name: 'my-guide-name'
  title: 'My Guide Title'
  subtitle: 'A short subtitle'
  authors: ['Your Name']
  category: 'Some Category'
  publicationDate: '2026-05-02'
  versions:
    '6':
      sourcePath: guides/my-guide-name/v6
      publicationDate: '2026-05-02'
      tags: ['grails6', 'topic']
      sampleRef:
        repo: 'grails-guides/my-guide-name'
        branch: 'grails6'
        sha: '<40-char SHA from grails-guides/my-guide-name>'
```

### Step-by-step

#### Tier 1 - Documentation only

1. Create `guides/<name>/v<N>/{guide,snippets}` (the `snippets/` directory can stay empty or be omitted).
2. Write your `guide/index.adoc` and chapter files.
3. Author `manifest.yml` and `toc.yml` (no `githubSlug`/`githubBranch`).
4. Add the registry entry to `conf/guides.yml` without a `sampleRef` block.
5. Validate locally: `./gradlew validateGuides -PvalidationMode=both`.
6. Render locally: `./gradlew renderGuide_<name>_<N>` and open `build/dist/guides/<name>/<N>/guide/index.html`.
7. Open a PR against this repository.

#### Tier 2 - Initial code only

1. Request a sample-app repository (see [Requesting a sample-app repository](#requesting-a-sample-app-repository)).
2. Push your starting-point app to `grails-guides/<name>` under an `initial/` directory on the appropriate `grails<N>` branch.
3. Tag the exact commit your guide will reference.
4. In `apache/grails-static-website`, create the guide source tree as in Tier 1.
5. Vendor any code your guide includes via `include::{sourceDir}/snippets/...[]` into `guides/<name>/v<N>/snippets/` (the renderer's `inlineSnippetIncludes` preprocessor reads these).
6. Add the registry entry with a `sampleRef` pointing at the SHA from step 3.
7. Validate, render, open PR.

#### Tier 3 - Initial and complete code

1-3. Same as Tier 2 but the upstream repository contains BOTH `initial/` AND `complete/` directories.

4-7. Same as Tier 2. Your guide can include snippets from either directory via `include::{sourceDir}/initial/...` or `include::{sourceDir}/complete/...`.

### Requesting a sample-app repository

Repositories under [`https://github.com/grails-guides`](https://github.com/grails-guides) are owned and provisioned by the Apache Grails PMC. Tier 2 and Tier 3 guides need one of these repositories before the guide can ship.

**Contact the PMC via the [community page](https://grails.apache.org/community.html):**

- **Dev mailing list**: subscribe by sending a blank email to [`dev-subscribe@grails.apache.org`](mailto:dev-subscribe@grails.apache.org), then post your request to [`dev@grails.apache.org`](https://lists.apache.org/list.html?dev%40grails.apache.org). Include the proposed guide name, a one-line description, and which Grails major version(s) the sample will target.
- **Slack**: join via [https://slack.grails.org](https://slack.grails.org) and ping the PMC in the appropriate channel.

A PMC member with org-admin access on `grails-guides` will create the repository, set the default branch (the latest `grails<N>` you target), and grant you push access. From there you push your `initial/` (and optionally `complete/`) tree as normal.

### Local validation checklist

Before opening the PR:

```bash
# Schema check on conf/guides.yml
./gradlew validateGuides -PvalidationMode=both

# Single-guide render (faster than buildGuides)
./gradlew renderGuide_<name>_<version>

# Full corpus + verification harness
./gradlew buildAllGuides
./gradlew verifyAllGuides
```

Open the rendered HTML directly in a browser - relative CSS makes file:// URLs work.

## Blog Posts

### Posts location

Write blog posts in Markdown under [`posts/`](posts/).

### Blog post metadata

A post supports metadata at the top of the document. Use it for title, description, publication date, and other display fields. Metadata is separated from the body by three dashes, and the metadata values can be referenced in the body via `[%fieldname]`.

A typical blog post looks like:

```markdown
---
title: Deploying Grails 3.1 Applications to JBoss 6.4 EAP
date: May 26, 2016
description: Learn necessary configuration differences to deploy Grails 3.1 applications to JBoss 6.4 EAP
author: Graeme Rocher
image: 2016-05-26.jpg
---

# [%title]

[%author]

[%date]

We had [previously](https://grails.io/post/142674392718/deploying-grails-3-to-wildfly-10) described how to deploy Grails 3.1 applications to WildFly 10, which is where all of the "cutting edge" work happens in the JBoss world.

The process to deploy Grails 3.1 applications to JBoss 6.4 EAP is largely similar, with some minor configuration differences.
```

#### Text Expander snippets

If you write to the Grails blog frequently, consider creating a [Text Expander](https://textexpander.com) snippet:

![TextExpander snippet example](docs/textexpander.png)

#### `title`

Used as the window title, the card title, the blog post main header, and in social cards.

#### `description`

Used as the HTML meta description tag and in social cards.

#### `date`

Publication date - drives ordering in the blog index, the displayed date in the UI, and the RSS feed entry date. Two accepted formats:

- `MMM d, yyyy` (e.g. `April 9, 2020`)
- `MMM d, yyyy HH:mm` (e.g. `April 9, 2020 09:00`)

**Posts dated in the future are scheduled.** The cron-driven publish workflow runs daily and ships scheduled posts when their date arrives.

#### Background image

Reference a background image from `assets/bgimages/` via the `image` metadata key:

```markdown
---
image: 2018-05-23.jpg
---
```

Place the source files under [`assets/bgimages/`](assets/bgimages/).

### Tags

Prefix tags with `#`:

```markdown
Tags: #angular
```

- Webinar on-demand recordings should be tagged `webinar`.
- Release announcements should be tagged `release`.
- Check the [list of existing tags](https://grails.apache.org/blog/index.html) and reuse them where reasonable.

#### Code highlighting

If your post contains code samples, opt into the Prism highlighter via metadata:

```markdown
---
CSS: [%url]/stylesheets/prism.css
JAVASCRIPT: [%url]/javascripts/prism.js
---

# [%title]
```

#### Embedded video

Set the `video` metadata to embed a YouTube video. Use a URL of the form `https://www.youtube.com/watch?v=...`:

```markdown
---
title: JSON Views
date: April 1, 2016
description: Jeff Scott Brown uses music examples to probe JSON views.
author: Jeff Scott Brown
image: 2016-04-01-2.jpg
video: https://www.youtube.com/watch?v=XnRNfDGkBVg
---

# [%title]

[%author]

[%date]

Tags:

[%description]
```

## Recording a release

When a new Grails version is published, append it to [`conf/releases.yml`](conf/releases.yml) via the `recordRelease` Gradle task:

```bash
./gradlew recordRelease -PreleaseVersion=7.0.0
```

This is also wired into the [`release.yml`](.github/workflows/release.yml) workflow, which is invoked manually from the Actions tab with the version as input.

## Assets (fonts, stylesheets, images, JavaScript)

All static assets used by the main site live under [`assets/`](assets/). Assets used by the guides site live under [`guides/resources/`](guides/resources/).

## Contributing

For broader Grails contribution discussion and questions:

- [Community page](https://grails.apache.org/community.html)
- [Dev mailing list](https://lists.apache.org/list.html?dev%40grails.apache.org)
- [Slack](https://slack.grails.org)
