package demo

import grails.gorm.transactions.Transactional
import groovy.transform.CompileStatic

@CompileStatic
class UserService {
    RoleDataService roleDataService

    UserRoleDataService userRoleDataService

    UserDataService userDataService

    @Transactional
    User save(String username, String password, String authority) {
        Role role = roleDataService.findByAuthority(authority)
        if ( !role ) {
            role = roleDataService.saveByAuthority(authority)
        }
        User user = userDataService.save(username, password)
        userRoleDataService.save(user, role)
        user
    }
}