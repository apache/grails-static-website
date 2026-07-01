package example

import spock.lang.Specification
import spock.lang.Unroll

class CustomerRequestStatusSpec extends Specification {

    @Unroll
    void "#status terminal flag is #expected"() {
        expect:
        status.terminal == expected

        where:
        status                            || expected
        CustomerRequestStatus.SUBMITTED   || false
        CustomerRequestStatus.IN_PROGRESS || false
        CustomerRequestStatus.COMPLETED   || true
        CustomerRequestStatus.CANCELLED   || true
    }

    void "the enum exposes exactly the four expected states"() {
        expect:
        CustomerRequestStatus.values()*.name() as Set ==
                ['SUBMITTED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED'] as Set
    }
}
