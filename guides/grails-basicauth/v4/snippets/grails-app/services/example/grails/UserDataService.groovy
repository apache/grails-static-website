package example.grails

import grails.gorm.services.Service

@Service(User)
interface UserDataService {

    User save(String username, String password, boolean enabled, boolean accountExpired, boolean accountLocked, boolean passwordExpired)

    void delete(Serializable id)

    int count()
}