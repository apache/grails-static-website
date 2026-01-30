package org.grails.documentation;

import groovy.transform.CompileDynamic;
import groovy.transform.CompileStatic;
import groovy.xml.MarkupBuilder
import org.grails.utils.MarkdownUtils
import org.yaml.snakeyaml.Yaml;

@CompileStatic
class ProfilesPage {

    @CompileDynamic
    static String mainContent(File profiles) {
        StringWriter writer = new StringWriter()
        MarkupBuilder html = new MarkupBuilder(writer)
        html.div( class:'headerbar chalicesbg'){
            div( class:'content'){
                h1 'Profiles'
            }
        }
        Collection<ProfilesCategory> categories = categories(profiles)
        html.div(class: 'content') {
            div(class: 'twocolumns') {
                div(class: 'odd column') {
                    mkp.yieldUnescaped(renderCategory(categories.find { it.title == 'Profiles' }))
                }
                div(class: 'column') {
                    mkp.yieldUnescaped(renderCategory(categories.find { it.title == 'Plugin Profiles' }))
                    mkp.yieldUnescaped(renderCategory(categories.find { it.title == 'Third-Party Profiles' }))

                }
            }
        }
        writer.toString()
    }

    @CompileDynamic
    private static Collection<ProfilesCategory> categories(File modules) {
        Yaml yaml = new Yaml()
        Map model = yaml.load(modules.newDataInputStream())
        Map<String, ProfilesCategory> categories = [:]
        model['profiles'].collect { k, v ->
            if (!categories.containsKey(v.category)) {
                ProfilesCategory cat = new ProfilesCategory()
                if (v.category) {
                    cat.title = v.category
                }
                if (v.image) {
                    cat.image = v.image
                }
                if (v.description) {
                    cat.description = v.description
                }
                categories[v.category] = cat
            }
            Profile item = new Profile()
            item.url = v.url
            item.title = v.title
            categories[v.category].profiles << item
        }
        categories.values()
    }

    @CompileDynamic
    private static String renderCategory(ProfilesCategory cat) {
        StringWriter writer = new StringWriter()
        MarkupBuilder html = new MarkupBuilder(writer)
        if (cat) {
            html.div(class: 'guidegroup') {
                div(class: 'guidegroupheader') {
                    html.img(src: cat.image, alt: cat.title)
                    h2 cat.title
                }
                ul {
                    if (cat.description) {
                        li(class: 'legend') {
                            mkp.yieldUnescaped MarkdownUtils.htmlFromMarkdown(cat.description)
                                    .replaceAll('<p>', '')
                                    .replaceAll('</p>', '')
                        }
                    }
                    cat.profiles.each { item ->
                        li {
                            a(href: item.url, item.title)
                        }
                    }
                }
            }
        }
        writer.toString()
    }

    static class ProfilesCategory {
        String title
        String image
        String description
        List<Profile> profiles = []
    }

    static class Profile {
        String title
        String url
    }
}
