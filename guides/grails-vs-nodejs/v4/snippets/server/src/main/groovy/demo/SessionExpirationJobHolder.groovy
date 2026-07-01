package demo


import groovy.time.TimeCategory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.scheduling.annotation.Scheduled

class SessionExpirationJobHolder {

    @Autowired
    SimpMessagingTemplate brokerMessagingTemplate //<1>

    @Value('${timeout.minutes}') //<2>
    Integer timeout

    @Scheduled(cron = "0 * * * * *") //<3>
    void findExpiredSessions() {
        Date timeoutDate
        use( TimeCategory ) { //<4>
            timeoutDate = new Date() - timeout.minutes
        }

        User.withTransaction {
            List<User> expiredUsers = User.where { //Query for loggedIn users with a lastLogin date after the timeout limit
                lastLogin != null
                lastLogin < timeoutDate
            }.list()

            //Iterate over the expired users
            expiredUsers.each { user ->
                user.lastLogin = null //Reset lastLogin date
                user.save(flush: true)

                //<5>
                brokerMessagingTemplate.convertAndSend "/topic/${user.username}".toString(), "logout"
            }
        }
    }
}