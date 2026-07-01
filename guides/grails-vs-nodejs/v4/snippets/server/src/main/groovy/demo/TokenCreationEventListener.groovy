package demo

import org.springframework.context.ApplicationListener
import grails.plugin.springsecurity.rest.RestTokenCreationEvent

class TokenCreationEventListener implements ApplicationListener<RestTokenCreationEvent> {

    void onApplicationEvent(RestTokenCreationEvent event) { //<1>

        User.withTransaction { //<2>
            User user = User.where { username == event.principal.username }.first()
            user.lastLogin = new Date()
            user.save(flush: true)
        }
    }
}