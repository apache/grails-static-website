package demo

import grails.gorm.services.Service

@Service(Notification)
interface NotificationService {
    int count()
    void deleteByEmail(String email)
}