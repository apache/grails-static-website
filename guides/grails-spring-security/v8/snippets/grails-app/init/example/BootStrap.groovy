package example

class BootStrap {

    def init = { servletContext ->
        environments {
            development {
                Role.withTransaction {
                    if (Role.count() == 0) {
                        def userRole = new Role(authority: 'ROLE_USER').save(failOnError: true)
                        def adminRole = new Role(authority: 'ROLE_ADMIN').save(failOnError: true)

                        def user = new User(username: 'user', password: 'password', enabled: true).save(failOnError: true)
                        UserRole.create(user, userRole, true)

                        def admin = new User(username: 'admin', password: 'password', enabled: true).save(failOnError: true)
                        UserRole.create(admin, adminRole, true)
                        UserRole.create(admin, userRole, true)
                    }

                    if (Resource.count() == 0) {
                        new Resource(name: 'Public Overview', description: 'Readable without authentication').save(failOnError: true)
                        new Resource(name: 'Admin Console', description: 'Restricted to administrators').save(failOnError: true)
                    }
                }
            }
        }
    }

    def destroy = {
    }
}
