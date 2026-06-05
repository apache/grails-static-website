package example

import spock.lang.Specification

class WorkOrderStatusSpec extends Specification {

    void "the enum exposes exactly the three expected states in lifecycle order"() {
        expect:
        WorkOrderStatus.values()*.name() == ['OPEN', 'PLANNED', 'COMPLETED']
    }
}
