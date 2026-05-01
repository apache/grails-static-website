package org.codenarc.rule.grails

import groovy.transform.CompileStatic
import org.codehaus.groovy.ast.AnnotatedNode
import org.codehaus.groovy.ast.AnnotationNode
import org.codehaus.groovy.ast.ImportNode
import org.codehaus.groovy.ast.ModuleNode
import org.codenarc.rule.AbstractAstVisitor
import org.codenarc.rule.AbstractAstVisitorRule

@CompileStatic
class GrailsTransactionalRule extends AbstractAstVisitorRule { // <1>
    int priority = 2 // <2>
    String name = 'GrailsTransactional' // <3>
    Class astVisitorClass = GrailsTransactionalVisitor // <4>
}

@CompileStatic
class GrailsTransactionalVisitor extends AbstractAstVisitor { // <5>

    private static final String SPRING_TRANSACTIONAL = 'org.springframework.transaction.annotation.Transactional'
    private static final String ERROR_MSG = 'Do not use Spring @Transactional, use @grails.gorm.transactions.Transactional instead'

    @Override
    void visitAnnotations(AnnotatedNode node) { // <6>
        node.annotations.each { AnnotationNode annotationNode ->
            String annotation = annotationNode.classNode.text
            if (annotation == SPRING_TRANSACTIONAL) {
                addViolation(node, ERROR_MSG)
            }
        }

        super.visitAnnotations(node)
    }

    @Override
    void visitImports(ModuleNode node) { // <7>
        node.imports.each { ImportNode importNode ->
            String importClass = importNode.className

            if (importClass == SPRING_TRANSACTIONAL) {
                node.lineNumber = importNode.lineNumber // <8>
                addViolation(node, ERROR_MSG)
            }
        }

        super.visitImports(node)
    }
}
