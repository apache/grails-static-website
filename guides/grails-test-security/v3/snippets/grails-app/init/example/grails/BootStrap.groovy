package example.grails

import grails.compiler.GrailsCompileStatic

@GrailsCompileStatic
class BootStrap {

    AnnouncementService announcementService

    UserService userService

    SecurityRoleService securityRoleService

    UserSecurityRoleService userSecurityRoleService

    def init = { servletContext ->

        List<String> authorities = ['ROLE_BOSS', 'ROLE_EMPLOYEE']
        authorities.each { String authority ->
            if ( !securityRoleService.findByAuthority(authority) ) {
                securityRoleService.save(authority)
            }
        }

        if ( !userService.findByUsername('sherlock') ) {
            User u = userService.save('sherlock', 'elementary')
            userSecurityRoleService.save(u, securityRoleService.findByAuthority('ROLE_BOSS'))
        }

        if ( !userService.findByUsername('watson') ) {
            User u = userService.save('watson', '221Bbakerstreet')
            userSecurityRoleService.save(u, securityRoleService.findByAuthority('ROLE_EMPLOYEE'))
        }

        announcementService.save('The Hound of the Baskervilles')
    }
    def destroy = {
    }
}
