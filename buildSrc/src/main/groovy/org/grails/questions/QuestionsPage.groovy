package org.grails.questions

import groovy.xml.MarkupBuilder
import org.grails.utils.MarkdownUtils
import org.yaml.snakeyaml.Yaml

class QuestionsPage {

    static String mainContent(File questions) {
        def writer = new StringWriter()
        def html = new MarkupBuilder(writer)
        def yaml = new Yaml()
        def model = yaml.load(questions.newDataInputStream())
        def questionList = model['questions'].collect {
            new Question(it as Map)
        }
        html.div(class: 'headerbar chalicesbg') {
            html.div(class: 'content') {
                h1('Questions')
            }
        }
        html.div(class: 'content') {
            article(id: 'questions') {
                for (Question question : questionList) {
                    div(class: 'question', id: question.slug) {
                        h2(class: 'columnheader', question.title)
                        mkp.yieldUnescaped(
                                MarkdownUtils.htmlFromMarkdown(question.answer)
                        )
                    }
                }
            }
        }
        writer.toString()
    }
}
