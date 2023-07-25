package org.grails.plugin

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.xml.MarkupBuilder
import org.grails.gradle.PluginsTask
import org.grails.guides.TagUtils
import org.grails.tags.Tag
import org.grails.tags.TagCloud
import java.time.format.DateTimeFormatter
import java.util.stream.Collectors


@CompileStatic
class PluginsPage {
    private static final Comparator<Plugin> COMPARE_BY_NAME = new Comparator<Plugin>() {
        @Override
        int compare(Plugin o1, Plugin o2) {
            o1.name.toLowerCase() <=> o2.name.toLowerCase()
        }
    }

    private static final Comparator<Plugin> COMPARE_BY_GITHUB_STARS = new Comparator<Plugin>() {
        @Override
        int compare(Plugin o1, Plugin o2) {
            o2.githubStars <=> o1.githubStars
        }
    }
    private static final Comparator<Plugin> COMPARE_BY_UPDATED = new Comparator<Plugin>() {
        @Override
        int compare(Plugin o1, Plugin o2) {
            o2.updated <=> o1.updated
        }
    }
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("MMM d, YYYY");

    @CompileDynamic
    static String mainContent(String siteUrl,
                              List<Plugin> plugins,
                              String title) {
        StringWriter writer = new StringWriter()
        MarkupBuilder html = new MarkupBuilder(writer)
        html.div(class: 'headerbar chalicesbg') {
            div(class: 'content') {
                h1 'Grails Plugins'
            }
        }
        html.div(class: 'content') {
            if (title == 'Grails Plugins') {
                div(class: 'twocolumns') {
                    div(class: 'column') {
                        mkp.yieldUnescaped latestPlugins(siteUrl, plugins)
                    }
                    div(class: 'column') {
                        mkp.yieldUnescaped topRatedPlugins(siteUrl, plugins)
                    }
                }
            } else {
                html.div(class: "breadcrumbs") {
                    a(href: siteUrl + "/plugins.html", "Grails Plugins")
                    span(' » ')
                    span(title)
                }
            }
            mkp.yieldUnescaped renderTwoColumnsPlugins(siteUrl, plugins)
            if (title == 'Grails Plugins') {
                div(class: 'twocolumns') {
                    div(class: 'column') {
                        mkp.yieldUnescaped linksMenu(siteUrl)
                        mkp.yieldUnescaped pluginsByOwner(siteUrl, plugins)
                    }
                    div(class: 'column') {
                        mkp.yieldUnescaped pluginsByTag(siteUrl, plugins)
                    }
                }
            }
        }
        writer.toString()
    }


    @CompileDynamic
    static String renderTwoColumnsPlugins(String siteUrl,List<Plugin> plugins) {
        StringWriter writer = new StringWriter()
        MarkupBuilder html = new MarkupBuilder(writer)
        html.div {
            h3(class: "columnheader", "Grails Plugins")
            plugins.sort(COMPARE_BY_NAME)
            int index = 0
            int page = 2
            do {
                List<Plugin> l = (plugins.size() > (index + page)) ?
                        plugins.subList(index, index + page) :
                        plugins.subList(index, plugins.size())
                l = l.reverse()
                if (l.size() == 1) {
                    mkp.yieldUnescaped renderSinglePlugin(siteUrl, l[0])
                } else {
                    div(class: 'twocolumns') {
                        div(class: 'column') {
                            if (l.size() >= 1) {
                                mkp.yieldUnescaped renderPlugins(siteUrl, [l[0]])
                            }
                        }
                        div(class: 'column') {
                            if (l.size() >= 2) {
                                mkp.yieldUnescaped renderPlugins(siteUrl, [l[1]])
                            }
                        }
                    }
                }
                index += page
            } while(plugins.size() > index)
        }
        writer.toString()
    }

    @CompileDynamic
    static String linksMenu(String siteUrl) {
        List<Map<String, String>> links = [
                [url: siteUrl+"/legacy-plugins.html", title: "Legacy Plugins (Grails 1 & 2)"],
                [url: "https://grails.org/blog/2021-04-07-publish-grails-plugin-to-maven-central.html", title: "Publishing Guide"],
                [url: "https://github.com/grails/grails-plugins-metadata", title: "Portal on Github"],
        ]

        StringWriter writer = new StringWriter()
        MarkupBuilder html = new MarkupBuilder(writer)
        html.ul(class: 'guidegroup') {
            for (Map<String, String> link : links) {
                li {
                    a(href: link.url, link.title)
                }
            }
        }
        writer.toString()
    }

    @CompileDynamic
    static String renderPlugins(String siteUrl,List<Plugin> plugins) {
        StringWriter writer = new StringWriter()
        MarkupBuilder html = new MarkupBuilder(writer)
        html.div(class: 'plugins'){
            ul {
                for (plugin in plugins) {
                    mkp.yieldUnescaped renderSinglePlugin(siteUrl, plugin)
                }
            }
        }
        writer.toString()
    }

    @CompileDynamic
    static String pluginsByOwner(String siteUrl, List<Plugin> plugins) {
        StringWriter writer = new StringWriter()
        MarkupBuilder html = new MarkupBuilder(writer)
        html.div {
            mkp.yieldUnescaped createHeader('Plugins By Owner')
            Set<Tag> tags = TagUtils.populateTagsByPluginsOwners(plugins)
            mkp.yieldUnescaped TagCloud.tagCloud(siteUrl + "/plugins/owners", tags, false)
        }
        writer.toString()
    }

    @CompileDynamic
    static String topRatedPlugins(String siteUrl, List<Plugin> plugins) {
        List<Plugin> pluginsWithStars = plugins.stream()
                .filter(p -> p.githubStars != null)
                .sorted(COMPARE_BY_GITHUB_STARS)
                .collect(Collectors.toList())
        List<Plugin> topRatedPlugins = []
        for (Plugin plugin : pluginsWithStars) {
            if (topRatedPlugins.stream().noneMatch(p -> p.vcsUrl == plugin.vcsUrl)) {
                topRatedPlugins.add(plugin)
            }
        }
        topRatedPlugins = topRatedPlugins.take(5)
        StringWriter writer = new StringWriter()
        MarkupBuilder html = new MarkupBuilder(writer)
        html.div {
            mkp.yieldUnescaped createHeader('Top Rated Plugins')
            div {
                ul {
                    for (Plugin plugin : topRatedPlugins) {
                        mkp.yieldUnescaped renderSinglePlugin(siteUrl, plugin)
                    }
                }
            }
        }
        writer.toString()
    }

    @CompileDynamic
    static String latestPlugins(String siteUrl, List<Plugin> plugins) {
        StringWriter writer = new StringWriter()
        MarkupBuilder html = new MarkupBuilder(writer)
        html.div {
            mkp.yieldUnescaped createHeader('Latest Plugins')
            mkp.yieldUnescaped fetchLatestPlugins(siteUrl, plugins)
        }
        writer.toString()
    }

    @CompileDynamic
    static String pluginsByTag(String siteUrl, List<Plugin> plugins) {
        StringWriter writer = new StringWriter()
        MarkupBuilder html = new MarkupBuilder(writer)
        html.div {
            mkp.yieldUnescaped createHeader('Plugins By Tag')
            Set<Tag> tags = TagUtils.populateTagsByPlugins(plugins)
            mkp.yieldUnescaped TagCloud.tagCloud(siteUrl + "/plugins/tags", tags, false)
        }
        writer.toString()
    }

    @CompileDynamic
    static String renderSinglePlugin(String siteUrl, Plugin plugin) {
        StringWriter writer = new StringWriter()
        MarkupBuilder mb = new MarkupBuilder(writer)
        mb.li(class: 'plugin') {

//                ul(class: 'iconlinks') {
//                    a(href: plugin.vcsUrl) {
//                        if (plugin.vcsUrl.contains("bintray.com")) {
//                            img(src: "${siteUrl}/images/bintray.svg", width: 20)
//                        } else if (plugin.vcsUrl.contains("github.com")) {
//                            img(src: "${siteUrl}/images/github.svg", width: 20)
//                        } else {
//                            mkp.yield("Repository")
//                        }
//                    }
//                }
            if (plugin.vcsUrl) {
                h3 {
                    a(href: plugin.vcsUrl, plugin.name)
                }
            } else {
                h3 {
                    a(plugin.name)
                }
            }
            if (plugin.desc) {
                p plugin.desc
            }
            p {
                if (plugin.latestVersion) {
                    span plugin.latestVersion
                }
                mkp.yield(" published ")
                mkp.yield(FORMATTER.format(plugin.updated))

                if (plugin.owner) {
                    span"by "
                    a(href: "[%url]/plugins/owners/${plugin.owner.name}.html") {
                        mkp.yield(plugin.owner.name)
                    }
                }
            }
            if (plugin.labels) {
                ul(class: 'labels') {
                    for (String label : plugin.labels) {
                        li(class: 'label') {
                            a(href: "[%url]/plugins/tags/${label}.html") {
                                mkp.yield(label)
                            }
                        }
                    }
                }
            }
            if (plugin.githubStars) {
                div(class: 'githubstar') {
                    span(class: 'star') {
                        img(src: "${siteUrl}/images/small_githubstar.svg", height: 20)
                    }
                    span(class: 'count', plugin.githubStars)
                }
            }
        }
        writer.toString()
    }

    @CompileDynamic
    static String createHeader(String section) {
        StringWriter writer = new StringWriter()
        MarkupBuilder html = new MarkupBuilder(writer)
        html.h3(class: 'columnheader', section)
        writer.toString()
    }

    @CompileDynamic
    static String fetchLatestPlugins(String siteUrl, List<Plugin> plugins) {
        plugins.sort(COMPARE_BY_UPDATED)
        List<Plugin> topFive = plugins.take(5)
        renderLatestPlugins(siteUrl, topFive)
    }

    @CompileDynamic
    static String renderLatestPlugins(String siteUrl, List<Plugin> topFive) {
        StringWriter writer = new StringWriter()
        MarkupBuilder html = new MarkupBuilder(writer)
        html.ul {
            for (plugin in topFive) {
                mkp.yieldUnescaped renderSinglePlugin(siteUrl, plugin)
            }
        }
        writer.toString()
    }
}
