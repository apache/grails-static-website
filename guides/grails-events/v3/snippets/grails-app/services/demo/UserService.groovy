package demo

import grails.gorm.services.Service

@Service(User)
interface UserService {
    int count()
    void deleteByEmail(String email)
}