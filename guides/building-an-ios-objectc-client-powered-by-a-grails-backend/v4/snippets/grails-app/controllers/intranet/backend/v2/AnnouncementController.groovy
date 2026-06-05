package intranet.backend.v2

import grails.rest.RestfulController
import intranet.backend.Announcement

class AnnouncementController extends RestfulController<Announcement> {
    static namespace = 'v2'
    static responseFormats = ['json']

    def announcementService

    AnnouncementController() {
        super(Announcement)
    }

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        def announcements = announcementService.findAllIdAndTitleProjections(params)
        respond announcements, model: [("${resourceName}Count".toString()): countResources()]
    }
}
