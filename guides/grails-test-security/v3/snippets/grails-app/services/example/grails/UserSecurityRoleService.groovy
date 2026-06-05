package example.grails

import grails.gorm.services.Service

@Service(UserSecurityRole)
interface UserSecurityRoleService {

    UserSecurityRole save(User user, SecurityRole securityRole)
}
