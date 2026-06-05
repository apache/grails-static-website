package demo

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import grails.compiler.GrailsCompileStatic

@GrailsCompileStatic
@EqualsAndHashCode(includes='username')
class User implements Serializable {

    String username
    String password
    Date lastLogin = null //<1>
    boolean enabled = true
    boolean accountExpired
    boolean accountLocked
    boolean passwordExpired

    Set<Role> getAuthorities() {
        (UserRole.findAllByUser(this) as List<UserRole>)*.role as Set<Role>
    }

    static constraints = {
        password nullable: false, blank: false, password: true
        username nullable: false, blank: false, unique: true
        lastLogin nullable: true
    }

    static mapping = {
        table 'users'
        password column: '`password`', type: 'text'
        id name: 'username', generator: 'assigned'
        version false
    }
}

