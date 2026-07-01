package demo

import grails.gorm.services.Service

@Service(User)
interface UserService {

    User findByUsername(String username)

    User save(User user)
}