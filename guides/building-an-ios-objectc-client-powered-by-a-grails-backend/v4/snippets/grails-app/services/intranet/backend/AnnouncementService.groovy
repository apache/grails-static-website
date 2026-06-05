package intranet.backend

import grails.gorm.transactions.Transactional

@Transactional(readOnly = true)
class AnnouncementService {

    List<Map> findAllIdAndTitleProjections(Map params) {
        def c = Announcement.createCriteria()
        def announcements = c.list(params) {
            projections {
                property('id')
                property('title')
            }
        }.collect { [id: it[0], title: it[1]] } as List<Map>
    }
}