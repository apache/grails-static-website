package com.ociweb.quickcasts

import spock.lang.Specification
import grails.testing.gorm.DomainUnitTest

/**
 * See the API for {@link grails.test.mixin.domain.DomainClassUnitTestMixin} for usage instructions
 */
class QuickcastSpec extends Specification implements DomainUnitTest<Quickcast> {

    void "test description is optional"() {
        expect:
            new Quickcast(description: null).validate(['description'])
    }
}
