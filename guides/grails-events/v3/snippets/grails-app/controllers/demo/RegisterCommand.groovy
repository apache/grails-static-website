package demo

import grails.validation.Validateable

class RegisterCommand implements Validateable {
    String firstName
    String lastName
    String email

    static constraints = {
        firstName nullable: false
        lastName nullable: false
        email nullable: false, email: true
    }

    Object asType(Class clazz) {
        if (clazz == User) {
            def user = new User()
            copyProperties(this, user)
            return user
        }
        else {
            super.asType(clazz)
        }
    }

    def copyProperties(source, target) {
        source.properties.each { key, value ->
            if (target.hasProperty(key) && !(key in ['class', 'metaClass'])) {
                target[key] = value
            }
        }
    }
}
