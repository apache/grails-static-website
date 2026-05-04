package example

import grails.persistence.Entity

@Entity
class Task {

    String title
    Boolean done = false
    Date dateCreated

    static constraints = {
        title blank: false, maxSize: 255
    }

    static mapping = {
        sort 'dateCreated'
        order 'desc'
    }
}
