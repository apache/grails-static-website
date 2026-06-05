package demo

import grails.events.annotation.gorm.Listener
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.grails.datastore.mapping.engine.event.PostInsertEvent
import org.grails.orm.hibernate.HibernateDatastore
import org.springframework.beans.factory.annotation.Autowired

@CompileStatic
@Slf4j
class UserInsertedListener {

    @Autowired
    HibernateDatastore hibernateDatastore

    @Autowired
    DatabaseProvisioningService databaseProvisioningService

    @Listener(User) // <1>
    void onUserPostInsertEvent(PostInsertEvent event) { // <2>
        String username = event.getEntityAccess().getPropertyValue("username")
        DatabaseConfiguration databaseConfiguration = databaseProvisioningService.findDatabaseConfigurationByUsername(username) // <3>
        hibernateDatastore.getConnectionSources().addConnectionSource(databaseConfiguration.dataSourceName, databaseConfiguration.configuration) // <4>
    }
}