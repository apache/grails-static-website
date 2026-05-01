package intranet.backend.v1

import grails.rest.RestfulController
import intranet.backend.Announcement

class AnnouncementController extends RestfulController<Announcement> {
    static namespace = 'v1' // <1>
    static responseFormats = ['json'] // <2>

    AnnouncementController() {
        super(Announcement)
    }
}
