package example

import grails.testing.mixin.integration.Integration
import grails.gorm.transactions.Rollback
import spock.lang.Specification

@Integration
@Rollback
class RecordLabelIntegrationSpec extends Specification {

    void 'record labels persist in PostgreSQL'() {
        when:
        RecordLabel.withTransaction {
            new RecordLabel(name: 'Integration Label').save(flush: true, failOnError: true)
        }

        then:
        RecordLabel.count() >= 1
        RecordLabel.findByName('Integration Label') != null
    }
}
