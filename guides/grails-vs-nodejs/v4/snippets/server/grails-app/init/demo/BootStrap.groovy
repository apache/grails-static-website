package demo

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

@Slf4j
@CompileStatic
class BootStrap {

    UserService userService
    UserRoleService userRoleService
    RoleService roleService
    IceCreamService iceCreamService

    def init = { servletContext ->
        log.info "Loading database..."

        if (!iceCreamService.count()) {

            List<Long> ids = []
            for (String flavor : ['vanilla', 'chocolate', 'strawberry']) {
                IceCream iceCream = iceCreamService.saveIcream(flavor)
                ids << iceCream.id
            }
            log.info "Inserted records with ids ${ids.join(',')}"
        }

        if (!roleService.count()) {
            Role role = roleService.saveRole( 'ROLE_USER')
            log.info "Inserted role..."

            User user = userService.createUser('sherlock', 'secret')
            log.info "Inserted user..."

            userRoleService.saveUserRole(user, role)
            log.info "Associated user with role..."
        }
    }
    def destroy = {
    }
}
