/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package website.qa

import groovy.transform.CompileStatic
import groovy.transform.ToString

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

/**
 * Structural comparison harness for guide rendering. Compares a locally
 * rendered HTML page against a reference snapshot (typically a saved copy
 * of the legacy {@code https://guides.grails.org/...} output) and returns
 * a {@link Report} of structural deltas.
 *
 * <p>Compares (as listed in {@code phase-8-vendor-plan.md}):</p>
 * <ol>
 *   <li>Heading tree (h1-h6 text + hierarchy)</li>
 *   <li>Section count</li>
 *   <li>Anchor IDs (deep-link parity)</li>
 *   <li>Code-block count</li>
 *   <li>Image count</li>
 * </ol>
 *
 * <p>This is a <em>diagnostic</em> harness, not a strict pass/fail gate.
 * It surfaces gaps so the renderer config (templates, attributes, includes)
 * can be iterated until the rendered output matches legacy structure.</p>
 */
@CompileStatic
class AdHocFixtureDiff {

    @ToString(includeNames = true)
    static class Metrics {
        int totalHeadings
        int h1
        int h2
        int h3
        int h4
        int h5
        int h6
        int sections
        int anchors
        int codeBlocks
        int images
        Set<String> headingTexts = [] as LinkedHashSet
        Set<String> anchorIds = [] as LinkedHashSet
    }

    @ToString(includeNames = true)
    static class Report {
        File localFile
        File baselineFile
        Metrics local
        Metrics baseline
        List<String> differences = []
        List<String> commonAnchors = []
        List<String> localOnlyAnchors = []
        List<String> baselineOnlyAnchors = []

        boolean isStructurallyEquivalent() {
            differences.isEmpty()
        }

        String toHumanReport() {
            StringBuilder sb = new StringBuilder()
            sb << '# Renderer Parity Report\n\n'
            sb << "Local: `${localFile?.absolutePath}`\n"
            sb << "Baseline: `${baselineFile?.absolutePath}`\n\n"
            sb << '## Structural metrics\n\n'
            sb << '| Metric | Local | Baseline | Delta |\n'
            sb << '|---|---:|---:|---:|\n'
            sb << "| Total headings | ${local.totalHeadings} | ${baseline.totalHeadings} | ${local.totalHeadings - baseline.totalHeadings} |\n"
            sb << "| h1 | ${local.h1} | ${baseline.h1} | ${local.h1 - baseline.h1} |\n"
            sb << "| h2 | ${local.h2} | ${baseline.h2} | ${local.h2 - baseline.h2} |\n"
            sb << "| h3 | ${local.h3} | ${baseline.h3} | ${local.h3 - baseline.h3} |\n"
            sb << "| h4 | ${local.h4} | ${baseline.h4} | ${local.h4 - baseline.h4} |\n"
            sb << "| Sections | ${local.sections} | ${baseline.sections} | ${local.sections - baseline.sections} |\n"
            sb << "| Anchor IDs | ${local.anchors} | ${baseline.anchors} | ${local.anchors - baseline.anchors} |\n"
            sb << "| Code blocks | ${local.codeBlocks} | ${baseline.codeBlocks} | ${local.codeBlocks - baseline.codeBlocks} |\n"
            sb << "| Images | ${local.images} | ${baseline.images} | ${local.images - baseline.images} |\n\n"

            sb << '## Anchor-ID overlap\n\n'
            sb << "- Common: ${commonAnchors.size()}\n"
            sb << "- Local-only: ${localOnlyAnchors.size()}\n"
            sb << "- Baseline-only: ${baselineOnlyAnchors.size()}\n"

            if (!localOnlyAnchors.isEmpty()) {
                sb << '\n### Local-only anchors (sample)\n\n'
                localOnlyAnchors.take(20).each { sb << "- `${it}`\n" }
                if (localOnlyAnchors.size() > 20) {
                    sb << "- ... ${localOnlyAnchors.size() - 20} more\n"
                }
            }
            if (!baselineOnlyAnchors.isEmpty()) {
                sb << '\n### Baseline-only anchors (broken deep-links if we cut over)\n\n'
                baselineOnlyAnchors.take(20).each { sb << "- `${it}`\n" }
                if (baselineOnlyAnchors.size() > 20) {
                    sb << "- ... ${baselineOnlyAnchors.size() - 20} more\n"
                }
            }

            sb << '\n## Differences\n\n'
            if (differences.isEmpty()) {
                sb << 'NONE -- structurally equivalent (within thresholds).\n'
            } else {
                differences.each { sb << "- ${it}\n" }
            }
            sb.toString()
        }
    }

    /**
     * Builds a {@link Metrics} from an HTML document.
     */
    static Metrics measure(Document doc) {
        Metrics m = new Metrics()
        m.h1 = doc.select('h1').size()
        m.h2 = doc.select('h2').size()
        m.h3 = doc.select('h3').size()
        m.h4 = doc.select('h4').size()
        m.h5 = doc.select('h5').size()
        m.h6 = doc.select('h6').size()
        m.totalHeadings = m.h1 + m.h2 + m.h3 + m.h4 + m.h5 + m.h6
        m.sections = doc.select('div.sect1, div.sect2, div.sect3, section').size()
        m.codeBlocks = doc.select('pre').size()
        m.images = doc.select('img').size()

        for (Element el : doc.select('h1, h2, h3, h4, h5, h6')) {
            String text = el.text()?.trim()
            if (text) m.headingTexts.add(text)
        }
        for (Element el : doc.select('[id]')) {
            String id = el.attr('id')?.trim()
            if (id) m.anchorIds.add(id)
        }
        m.anchors = m.anchorIds.size()
        m
    }

    /**
     * Compares two HTML files and returns a structural-difference report.
     * Threshold defaults are documented in {@code phase-8-vendor-plan.md}.
     *
     * @param localFile rendered locally (e.g. {@code build/dist/.../guide/single.html})
     * @param baselineFile reference snapshot (e.g. {@code buildSrc/src/test/resources/parity-baseline/.../index.html})
     */
    static Report compare(File localFile, File baselineFile) {
        Document localDoc = Jsoup.parse(localFile, 'UTF-8')
        Document baselineDoc = Jsoup.parse(baselineFile, 'UTF-8')

        Metrics local = measure(localDoc)
        Metrics baseline = measure(baselineDoc)

        Report report = new Report(
                localFile: localFile,
                baselineFile: baselineFile,
                local: local,
                baseline: baseline,
        )

        report.commonAnchors = sortedStringList(local.anchorIds.intersect(baseline.anchorIds))
        report.localOnlyAnchors = sortedStringList(local.anchorIds - baseline.anchorIds)
        report.baselineOnlyAnchors = sortedStringList(baseline.anchorIds - local.anchorIds)

        // Hard-fail conditions
        if (local.totalHeadings == 0) {
            report.differences.add('Local has zero headings -- renderer likely produced wrong file or empty output.')
        }
        if (baseline.totalHeadings > 0 && local.totalHeadings * 5 < baseline.totalHeadings) {
            report.differences.add("Heading-count gap is severe: local=${local.totalHeadings} baseline=${baseline.totalHeadings}. Likely rendering only a TOC vs full single-page.".toString())
        }
        if (!report.baselineOnlyAnchors.isEmpty()) {
            report.differences.add("${report.baselineOnlyAnchors.size()} anchor IDs from the legacy site are missing locally -- deep-links to those would break post-cutover.".toString())
        }

        // Soft-warn conditions (still recorded as differences)
        int sectionDelta = Math.abs(local.sections - baseline.sections)
        int sectionMax = Math.max(local.sections, baseline.sections)
        if (sectionMax > 0 && (sectionDelta * 100 / sectionMax) > 5) {
            report.differences.add("Section count drift > 5%: local=${local.sections} baseline=${baseline.sections} (delta=${sectionDelta})".toString())
        }
        int codeDelta = Math.abs(local.codeBlocks - baseline.codeBlocks)
        int codeMax = Math.max(local.codeBlocks, baseline.codeBlocks)
        if (codeMax > 0 && (codeDelta * 100 / codeMax) > 5) {
            report.differences.add("Code-block count drift > 5%: local=${local.codeBlocks} baseline=${baseline.codeBlocks} (delta=${codeDelta})".toString())
        }
        int imgDelta = Math.abs(local.images - baseline.images)
        int imgMax = Math.max(local.images, baseline.images)
        if (imgMax > 0 && (imgDelta * 100 / imgMax) > 5) {
            report.differences.add("Image count drift > 5%: local=${local.images} baseline=${baseline.images} (delta=${imgDelta})".toString())
        }

        report
    }

    private static List<String> sortedStringList(Object collection) {
        List<String> result = []
        for (Object item : (collection as Iterable)) {
            if (item instanceof String) {
                result << (item as String)
            }
        }
        result.sort()
        result
    }
}
