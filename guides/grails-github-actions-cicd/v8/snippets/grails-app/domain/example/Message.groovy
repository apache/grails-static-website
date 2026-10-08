package example

import grails.persistence.Entity

@Entity
class Message {

    String content

    static constraints = {
        content nullable: false, blank: false, maxSize: 500
    }

    String toString() { content }
}
