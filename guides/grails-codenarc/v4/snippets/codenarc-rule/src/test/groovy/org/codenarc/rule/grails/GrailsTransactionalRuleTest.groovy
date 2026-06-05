package org.codenarc.rule.grails

import org.codenarc.rule.AbstractRuleTestCase
import org.codenarc.rule.Rule
import org.codenarc.rule.Violation
import org.junit.Test

class GrailsTransactionalRuleTest extends AbstractRuleTestCase { // <1>

    @Override
    protected Rule createRule() { // <2>
        return new GrailsTransactionalRule()
    }

    @Test
    void testGrailsTransactionalIsAllowedOnClassWithImport() {
        final SOURCE = ''' // <3> 
            import grails.gorm.transactions.Transactional

            @Transactional
            class TestService {
            }
        '''

        assertNoViolations(SOURCE) // <4>
    }

    @Test
    void testGrailsTransactionalIsAllowedOnClassWithFullpackageAnnotation() {
        final SOURCE = '''
            @grails.gorm.transactions.Transactional
            class TestService {
            }
        '''

        assertNoViolations(SOURCE)
    }

    @Test
    void testGrailsTransactionalIsAllowedOnMethodWithImport() {
        final SOURCE = '''
            import grails.gorm.transactions.Transactional

            class TestService {
                @Transactional
                void foo() {
                }
            }
        '''

        assertNoViolations(SOURCE)
    }

    @Test
    void testGrailsTransactionalIsAllowedOnMethodWithFullpackageAnnotation() {
        final SOURCE = '''
            class TestService {
                @grails.gorm.transactions.Transactional
                void foo() {
                }
            }
        '''

        assertNoViolations(SOURCE)
    }

    @Test
    void testSpringTransactionalIsNotAllowedOnClassWithImport() {
        final SOURCE = '''
            import org.springframework.transaction.annotation.Transactional

            @Transactional
            class TestService {
            }
        '''

        // <5>
        assertSingleViolation(SOURCE) { Violation violation ->
            violation.rule.priority == 2 &&
            violation.rule.name == 'GrailsTransactional'
        }
    }

    @Test
    void testSpringTransactionalIsNotAllowedOnClassWithFullpackageAnnotation() {
        final SOURCE = '''
            @org.springframework.transaction.annotation.Transactional
            class TestService {
            }
        '''

        assertSingleViolation(SOURCE) { Violation violation ->
            violation.rule.priority == 2 &&
            violation.rule.name == 'GrailsTransactional'
        }
    }

    @Test
    void testSpringTransactionalIsNotAllowedOnMethodWithImport() {
        final SOURCE = '''
            import org.springframework.transaction.annotation.Transactional

            class TestService {
                @Transactional
                void foo() {
                }
            }
        '''

        assertSingleViolation(SOURCE) { Violation violation ->
            violation.rule.priority == 2 &&
                violation.rule.name == 'GrailsTransactional'
        }
    }

    @Test
    void testSpringTransactionalIsNotAllowedOnMethodWithFullpackageAnnotation() {
        final SOURCE = '''
            class TestService {
                @org.springframework.transaction.annotation.Transactional
                void foo() {
                }
            }
        '''

        assertSingleViolation(SOURCE) { Violation violation ->
            violation.rule.priority == 2 &&
                violation.rule.name == 'GrailsTransactional'
        }
    }
}
