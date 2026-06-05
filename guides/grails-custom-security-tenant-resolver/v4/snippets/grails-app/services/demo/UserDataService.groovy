package demo

import grails.gorm.services.Service
import groovy.transform.CompileStatic

@CompileStatic
@Service(User)
interface UserDataService {
    User save(String username, String password)
    void delete(Serializable id)
    User findByUsername(String username)
}
