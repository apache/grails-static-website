package demo

import geb.spock.GebSpec
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration

@Integration
class RegisterControllerSpec extends GebSpec {

    NotificationService notificationService

    UserService userService

    @Rollback
    def "If you signup a User, an Event triggers which causes a Notification to be saved"() {
        when: 'you signup with a non existing user'
        SignUpPage page = to SignUpPage
        page.submit('Sergio', 'del Amo', 'delamos@email.com')

        then: 'the user gets created and a notification is saved due to the event being triggered'
        userService.count() == old(userService.count()) + 1
        notificationService.count() == old(notificationService.count()) + 1

        when: 'you try to signup with a user which is already in the database'
        page = to SignUpPage
        page.submit('Sergio', 'del Amo', 'delamos@email.com')

        then: 'The user is not saved and no event gets triggered'
        noExceptionThrown()
        userService.count() == old(userService.count())
        notificationService.count() == old(notificationService.count())

        cleanup:
        userService.deleteByEmail('delamos@email.com')
        notificationService.deleteByEmail('delamos@email.com')
    }
}
