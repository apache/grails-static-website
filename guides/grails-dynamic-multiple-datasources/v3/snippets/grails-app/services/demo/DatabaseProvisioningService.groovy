package demo

import groovy.transform.CompileStatic

@CompileStatic
class DatabaseProvisioningService {

    UserRoleService userRoleService

    List<DatabaseConfiguration> findAllDatabaseConfiguration() {
        List<String> usernames = userRoleService.findAllUsernameByAuthority(VillainService.ROLE_VILLAIN)
        usernames.collect { findDatabaseConfigurationByUsername(it) }
    }

    DatabaseConfiguration findDatabaseConfigurationByUsername(String username) {
        new DatabaseConfiguration(dataSourceName: username, configuration: configurationByUsername(username))
    }

    Map<String, Object> configurationByUsername(String username) {
        [
                'hibernate.hbm2ddl.auto':'none', // <1>
                'username': 'root', // <2>
                'password': 'root', // <2>
                'url':"jdbc:mysql://127.0.0.1:8889/$username" // <2>
        ] as Map<String, Object>
    }
}

