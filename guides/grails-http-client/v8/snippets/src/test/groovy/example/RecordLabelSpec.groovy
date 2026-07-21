package example

import grails.testing.gorm.DataTest
import spock.lang.Specification

class RecordLabelSpec extends Specification implements DataTest {

    Class<?>[] getDomainClassesToMock() {
        [RecordLabel] as Class[]
    }

    void 'name cannot be blank'() {
        when:
        RecordLabel recordLabel = new RecordLabel(name: '')

        then:
        !recordLabel.validate()
        recordLabel.errors['name'].code in ['blank', 'nullable']
    }

    void 'name must be unique'() {
        given:
        new RecordLabel(name: 'Motown').save(flush: true)

        when:
        RecordLabel duplicate = new RecordLabel(name: 'Motown')

        then:
        !duplicate.validate()
        duplicate.errors['name'].code == 'unique'
    }
}
