package example.grails

import geb.Page

class AnnouncementListingPage extends Page {
    static url = '/announcement/index'

    static at = {
        $('#list-announcement').text()?.contains 'Announcement List'
    }
}
