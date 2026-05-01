package demo

import groovy.transform.CompileStatic
import org.grails.orm.hibernate.HibernateDatastore

@CompileStatic
class BootStrap {

    HibernateDatastore hibernateDatastore
    DatabaseProvisioningService databaseProvisioningService

    def init = { servletContext ->
        for (DatabaseConfiguration databaseConfiguration : databaseProvisioningService.findAllDatabaseConfiguration() ) { // <1>
            hibernateDatastore.getConnectionSources().addConnectionSource(databaseConfiguration.dataSourceName, databaseConfiguration.configuration)
        }
    }

    def destroy = {
    }
}
