package example

import grails.persistence.Entity
import groovy.util.logging.Slf4j

@Slf4j
@Entity
class RecordLabel {

    String name

    static constraints = {
        name blank: false, nullable: false, maxSize: 100, unique: true
    }

    String toString() {
        name
    }
}
