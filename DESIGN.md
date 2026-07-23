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

# Apache Grails Website Design System

This document defines the reusable visual system for the guides catalogue. Implementations must place these tokens and components in `assets/stylesheets/screen.css` rather than introduce one-off styles. Catalogue tokens are scoped to the emitted `.guides-page` wrapper because the shared document template does not apply page metadata to the `<body>` element.

## 1. Atmosphere & Identity

The Apache Grails website is direct, technical, and welcoming. White and off-white reading surfaces keep dense documentation calm, Grails blue establishes structure, and the warm orange accent carries the framework's identity without overpowering the content. The signature is the chalice-led blue header paired with orange-topped white content cards.

## 2. Color

### Palette

| Role | Token | Value | Usage |
| --- | --- | --- | --- |
| Brand | `--guides-color-brand` | `#255AA8` | Section headers, version chips, category title links, focus outlines, Kapa modal title |
| Brand hover | `--guides-color-brand-hover` | `#1d4684` | Interactive brand surfaces on hover and focus |
| Brand soft | `--guides-color-brand-soft` | `rgba(37, 90, 168, 0.08)` | Featured catalogue atmosphere |
| Accent | `--guides-color-accent` | `#feb672` | Card top borders, hidden launcher text accent, catalogue REST icon, identity details |
| Launcher dark | Kapa widget only | `#3F4346` | Ask AI launcher / project chrome background (not a page surface) |
| Legacy tag gold | Existing `.tag-cloud` scale | `#F0C45A` | Existing occurrence-scaled tag navigation |
| Accent wash | `--guides-color-accent-wash` | `rgba(254, 182, 114, 0.2)` | Featured catalogue highlight |
| Surface | `--guides-color-surface` | `#ffffff` | Cards, inputs, panels |
| Page | `--guides-color-page` | `#F5F5F5` | Page ground and quiet dividers |
| Text | `--guides-color-text` | `#666666` | Body and metadata (AA on white/off-white) |
| Strong text | `--guides-color-text-strong` | `#4f4f4f` | Guide titles and high-emphasis labels |
| Tag chip text | `--guides-color-tag-text` | `#3d2914` | Dark ink on gold/orange tag chips |
| On brand | `--guides-color-on-brand` | `#ffffff` | Text on blue surfaces |
| Quiet border | `--guides-color-border` | `#d8d8d8` | Nonessential dividers and card boundaries |
| Control border | `--guides-color-control-border` | `#767676` | Input and control boundaries (3:1+ on white/off-white) |

### Rules

- Grails blue establishes hierarchy; orange is an accent, not a second background system.
- White cards sit on the off-white page ground.
- New solid catalogue colors used for text, backgrounds, borders, or controls must be declared in this table and as scoped CSS custom properties before use. Derived alpha shadows may reuse brand RGB values when their complete shadow value is documented in the depth table.
- Purple gradients and unrelated accent hues do not belong in this design system.

## 3. Typography

### Font stack

| Family | Usage |
| --- | --- |
| Roboto, Roboto Light, Roboto Medium | Catalogue UI, guide titles, metadata, navigation |
| Archia Light, Archia Thin, Archia Medium | Marketing display text elsewhere on the site |
| Inter | Long-form rendered guide articles only |
| JetBrains Mono | Code inside rendered guide articles only |

### Scale

| Level | Size | Weight | Line height | Usage |
| --- | --- | --- | --- | --- |
| Page title | `24px` | Medium | `1.2` | Blue header bar |
| Section | `18px` | Medium | `28px` | `.column-header` labels |
| Card title | `16px` | Medium | `1.35` | Guide and category cards |
| Body | `16px` | Regular | `1.5` | Catalogue introductions and controls |
| Metadata | `14px` | Regular | `1.4` | Date and category |
| Guide version chip | `12px` | Light | `1.2` | Version links inside guide cards |
| Version navigation chip | `16px` | Regular | `14px` | Legacy version-cloud navigation |

### Heading hierarchy (guides catalogue)

| Context | Rank | Notes |
| --- | --- | --- |
| Featured / version / catalogue / discovery section titles | `h2` | One section label each |
| Category grid card titles | `h3` | Nested under catalogue `h2` |
| Standalone category or tag page title | `h2` | Page-level heading when not nested |
| Latest Guides section | `h3` | Nested under discovery `h2` |
| Latest guide card titles | `h4` | Nested under Latest Guides `h3` |
| Featured / version guide card titles | `h3` | Nested under their section `h2` |
| Search result / no-result titles | `h3` | Nested under Find Guides `h2` |

Large headings may use balanced wrapping. Body copy should use natural wrapping and remain readable at 200 percent zoom.

## 4. Spacing & Layout

### Spacing tokens

| Token | Value | Usage |
| --- | --- | --- |
| `--guides-space-2` | `8px` | Chip gaps and tight relationships |
| `--guides-space-3` | `12px` | Compact list rows |
| `--guides-space-4` | `16px` | Default card and input padding |
| `--guides-space-5` | `24px` | Grid gaps and section rhythm |
| `--guides-space-6` | `32px` | Major section separation |
| Legacy section step | `50px` | Existing `.column-header` and page spacing |

### Grid

- Maximum content width is `1141px`, inherited from `.content`.
- The existing desktop breakpoint is `961px`; the catalogue adds a content-driven tablet breakpoint at `768px`.
- Catalogue and discovery layouts are one column below `768px`.
- Category cards use CSS multi-column masonry (`column-count` 1 / 2 / 3 at mobile / tablet / desktop) with `break-inside: avoid` so cards keep natural height instead of stretched equal-height grid rows.
- Browser mechanics such as `column-count`, percentages, and intrinsic sizing remain raw CSS rather than design tokens.

## 5. Components

### Featured catalogue

- **Structure:** contained guides header followed by a labelled `section`, blue section heading, lightly layered blue-orange atmosphere, introduction, guide-card grid.
- **Variants:** current featured major on the index; selected major on a version page.
- **Spacing:** section gap 6, card gap 3 or 4, card padding 4.
- **States:** links expose default, hover, active, visited, and visible focus behavior.
- **Accessibility:** the heading labels the section; source order places the promoted catalogue before discovery controls.
- **Motion:** inherited color transitions only; no decorative movement.

### Guide card

- **Structure:** title, metadata, text action inside a list item.
- **Variants:** featured and version cards use `h3.guides-card-title`; latest cards use `h4.guides-card-title` under the Latest Guides `h3`.
- **Spacing:** padding 4; title-to-meta and meta-to-action use space 2.
- **States:** focus within receives a clear brand outline; links retain underline feedback.
- **Accessibility:** titles remain text, metadata remains legible, and the action stays keyboard reachable.
- **Layout:** cards flow through the responsive guide-card grid.

### Category grid and category card

- **Structure:** labelled catalogue section (`h2`), multi-column masonry container, image-and-title card header (`h3` when linked in the grid), guide list.
- **Variants:** full catalogue and selected-major-only catalogue; standalone category pages keep an `h2` title.
- **Spacing:** column gap 5; compact list rows use spaces 3 and 4; cards stack with space 5 between blocks in a column.
- **States:** category heading links use `--guides-color-brand` / hover variant (AA on white).
- **Accessibility:** category images are decorative because the adjacent heading supplies the name.
- **Icons:** homepage blue surfaces keep shared `restapis.svg` (white); catalogue cards use `restapis-guides.svg` (orange `#feb672` on white).
- **Layout:** one column on mobile; two columns from `768px`; three columns from `961px`; cards never stretch to equal row height.

### Discovery

- **Structure:** labelled section containing search/latest guides and version/tag navigation.
- **Variants:** index with search and latest cards; version page with navigation only.
- **Spacing:** section gap 6 and column gap 5 or 6.
- **States:** search uses an explicit label, native input behavior, and visible `:focus` / `:focus-visible` outline.
- **Accessibility:** a separate visually hidden `#guides-search-status` node uses `role="status"`, `aria-live="polite"`, and `aria-atomic="true"`; JS sets `""` on reset, `"N guide(s) found"` on matches, and `"No results found"` otherwise. Visible search result headings are `h3` under Find Guides.
- **Layout:** one column below desktop; two columns from `961px` only when both columns contain content.

### Version chip

- **Structure:** direct `.align-left.guides-version-chip` child of `.multi-guide`, containing the link and hidden search tags.
- **Guide variant:** `12px` inherited Roboto Light text, `4px` radius, and compact `4px 10px` padding.
- **Version-cloud navigation:** legacy `16px` inherited Roboto Regular text, `3px` radius, and `7px 12px 5px` padding.
- **States:** both use blue default, darker blue hover, and the catalogue's visible keyboard outline.
- **Accessibility:** search indexing uses `classList.contains('align-left')` so extra chip classes remain compatible.

### Mobile navigation

- **Structure:** `#show-navigation-link` is a visible `button` that toggles `#top-menus.is-open`.
- **States:** closed (`Show Navigation`, `aria-expanded="false"`) and open (`Hide Navigation`, `aria-expanded="true"`).
- **Color:** open mobile panel background is `#666666` with white links (AA); desktop panel stays white.
- **Behavior:** `toggleNavigation()` toggles the `is-open` class only (no inline `display`); desktop `@media (min-width: 961px)` keeps `#top-menus` visible after mobile open/close then resize. Legacy `show()` remains for other callers.

### Kapa Ask AI launcher

- **Structure:** 44x44 control with 12px bottom/right offsets; accessible DOM text `"Ask AI"` with `0` font-size so the control stays icon-only visually.
- **Colors:** hidden launcher text may use accent `#feb672`; modal title uses brand blue `#255AA8` on white; launcher background uses documented dark `#3F4346`.
- **Attributes:** documented legacy `data-button-*` plus current `data-launcher-button-*` height/width/background/bottom/right/image/padding/border-radius; `data-button-*` aliases (`text-color`, `text-font-size`, `bg-color`, `position-bottom/right`) are used for compatibility.
- **Layout:** catalogue pages reserve bottom safe space; below `961px`, `footer` gets a `68px` right exclusion zone so sentences are not covered.

## 6. Motion & Interaction

| Type | Duration | Easing | Usage |
| --- | --- | --- | --- |
| Color feedback | `200ms` | ease in-out | Links and interactive surfaces |
| Button press | `100ms` | ease | Existing one-pixel pressed translation |
| Card shadow | `300ms` | ease | Existing reusable content cards |

- Motion communicates interaction only.
- New animation may use only `transform`, `opacity`, or color changes.
- `prefers-reduced-motion: reduce` sets `scroll-behavior: auto`, disables link/button transitions, and removes button press `transform` (including the navigation toggle).
- Every interactive element needs a visible focus state; hover cannot be the only cue. Search focus uses both `:focus` and `:focus-visible`.

## 7. Depth & Surface

The catalogue uses a restrained mixed strategy inherited from the site: white surfaces, an orange top rule for identity, and a very soft card shadow. Deeper shadows, glass effects, and heavy borders are not part of this surface.

| Token | Value | Usage |
| --- | --- | --- |
| `--guides-radius` | `4px` | Cards, chips, inputs |
| `--guides-shadow-card` | Blue-tinted soft elevation plus one-pixel grounding shadow | Guide and category cards |
| Section shadow | `0 8px 18px rgba(37, 90, 168, 0.14)` | Section title elevation derived from brand blue |
| Legacy version-navigation radius | `3px` | Existing version-cloud chips |
| Accent rule | `2px solid var(--guides-color-accent)` | Guide and category card tops |

## 8. Accessibility Constraints & Accepted Debt

### Constraints

- Target WCAG 2.2 AA with at least 4.5:1 contrast for normal text and 3:1 for large text and non-text controls.
- Keyboard users must reach every guide, category, version, and search control with a visible focus indicator.
- Users at 200 percent zoom and users on 375px-wide screens must receive a single readable column without horizontal scrolling of primary content.
- Reduced-motion users receive no non-essential transitions.
- The dense catalogue must lead with the promoted version and clear section labels to reduce memory and wayfinding load.
- Nested headings must not share rank with their parent section label.

### Accepted debt

| Item | Location | Why accepted | Owner / Exit |
| --- | --- | --- | --- |
| Legacy float layout | Non-guides `.two-columns` pages | Outside the Grails 8 catalogue scope | Replace when those pages are redesigned |
| Occurrence-scaled tag cloud sizes | `.tag1` through `.tag50` | Size scale retained; chip text forced to `--guides-color-tag-text` for AA | Revisit with a dedicated taxonomy UX change |
| Separate article typography | `body.guide` | Rendered guide reading system is intentionally distinct | Keep unless long-form documentation is redesigned |
