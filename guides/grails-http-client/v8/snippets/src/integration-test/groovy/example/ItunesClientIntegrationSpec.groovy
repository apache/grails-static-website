package example

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Specification

import org.springframework.beans.factory.annotation.Autowired

@Integration
@Rollback
class ItunesClientIntegrationSpec extends Specification {

    @Autowired
    ItunesClient itunesClient

    void 'declarative ItunesClient HTTP service is registered as a Spring bean'() {
        expect:
        itunesClient != null
        itunesClient instanceof ItunesClient
    }
}
