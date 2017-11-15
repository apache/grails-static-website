package org.grails.main.pages

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.xml.MarkupBuilder
import org.grails.Navigation
import org.grails.model.MenuItem
import org.grails.pages.Page

@CompileStatic
class SearchPage extends Page {

    String slug = 'search.html'
    String bodyClass = 'search'
    String title = 'Search'

    @Override
    MenuItem menuItem() {
        Navigation.searchMenuItem(grailsUrl())
    }

    @CompileDynamic
    @Override
    String mainContent() {
        StringWriter writer = new StringWriter()
        MarkupBuilder html = new MarkupBuilder(writer)
        html.div(class: 'content') {
            article(class: 'post') {
               p '''
                                You can search the Grails website, the Grails documentation, and the Grails APIs
                                with the Google Custom Search box below. Please type your search query and hit enter.'''
            div {
                style '''
                                    table.gsc-search-box td {
                                        vertical-align: top !important;
                                    }
                                    .gsc-completion-container {
                                        padding-top: 20px !important;
                                    }
                                    .gsc-input-box {
                                        height: 32px;
                                    }
                                    input.gsc-search-button, input.gsc-search-button-v2 {
                                        height: 30px;
                                        display: none;
                                    }
                                    .gs-title {
                                        line-height: 20px;
                                        font-weight: bold;
                                    }
                                    .gs-snippet {
                                        margin-left: 8px;
                                        line-height: 18px;
                                        font-family: "Open Sans", "Helvetica Neue", Helvetica, Arial, sans-serif;
                                    }
                                    .gsc-cursor-page {
                                        margin: 4px;
                                        padding: 4px;
                                        padding-left: 8px;
                                        padding-right: 8px;
                                        border: 1px solid gray;
                                    }
                                    .gsc-cursor-page:hover {
                                        color: white !important;
                                        background-color: #db4800 !important;
                                        text-decoration: none !important;
                                    }
                                    .gsc-cursor-current-page {
                                        background-color: #F2F2F2 !important;
                                        color: black !important;
                                    }
                                    .gsc-selected-option-container {
                                        width: 120px !important;
                                    }
                                    td.gsc-search-button {
                                        padding-top: 6px;
                                    }
                                    td.gsc-orderby-container {
                                        padding-right: 20px;
                                    }
                                    .gs-no-results-result .gs-snippet {
                                        font-weight: bold;
                                        color: #db4800;
                                        background-color: white;
                                        border: 0px;
                                    }
                                    .gsc-webResult, .gsc-result {
                                        font-family: "Open Sans", "Helvetica Neue", Helvetica, Arial, sans-serif;
                                    }
                                    .gsst_a, .gscb_a {
                                        margin-top: 3px;
                                    }
                                    .gs-per-result-labels {
                                        margin-left: 8px;
                                        margin-top: 4px;
                                    }
                                    .gs-per-result-labels:before {
                                        font-family: FontAwesome;
                                        content: '\\f02c';
                                        margin-right: 8px;
                                    }
                                    .gsc-tabHeader.gsc-tabhActive {
                                        border-color: #CCCCCC;
                                        border-bottom-color: #F2F2F2;
                                        background-color: #F2F2F2;
                                    }
                                    .gsc-tabsArea {
                                        border-bottom: 0;
                                    }
                                    a.gs-label, img.gs-image {
                                        margin-left: 5px;
                                    }
                                '''
                mkp.yieldUnescaped '''
<script>
  (function() {
    var cx = '013897913388943055015:l1yajhbenou';
    var gcse = document.createElement('script');
    gcse.type = 'text/javascript';
    gcse.async = true;
    gcse.src = 'https://cse.google.com/cse.js?cx=' + cx;
    var s = document.getElementsByTagName('script')[0];
    s.parentNode.insertBefore(gcse, s);
  })();
</script>
<gcse:search></gcse:search>
'''
                }
            }
        }
        writer.toString()
    }
}
