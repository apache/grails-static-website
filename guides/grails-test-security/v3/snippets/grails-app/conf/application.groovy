grails {
    plugin {
        springsecurity {
            rest {
                token {
                    storage {
                        jwt {
                            secret = 'pleaseChangeThisSecretForANewOne'
                        }
                    }
                }
            }
            securityConfigType = "InterceptUrlMap"  // <1>
            filterChain {
                chainMap = [
                [pattern: '/api/**',filters: 'JOINED_FILTERS,-anonymousAuthenticationFilter,-exceptionTranslationFilter,-authenticationProcessingFilter,-securityContextPersistenceFilter,-rememberMeAuthenticationFilter'],// <2>
                [pattern: '/**', filters: 'JOINED_FILTERS,-restTokenValidationFilter,-restExceptionTranslationFilter'] // <3>
                ]
            }
            userLookup {
                userDomainClassName = 'example.grails.User' // <4>
                authorityJoinClassName = 'example.grails.UserSecurityRole' // <4>
            }
            authority {
                className = 'example.grails.SecurityRole' // <4>
            }
            interceptUrlMap = [
                    [pattern: '/',                      access: ['permitAll']],
                    [pattern: '/error',                 access: ['permitAll']],
                    [pattern: '/index',                 access: ['permitAll']],
                    [pattern: '/index.gsp',             access: ['permitAll']],
                    [pattern: '/shutdown',              access: ['permitAll']],
                    [pattern: '/assets/**',             access: ['permitAll']],
                    [pattern: '/**/js/**',              access: ['permitAll']],
                    [pattern: '/**/css/**',             access: ['permitAll']],
                    [pattern: '/**/images/**',          access: ['permitAll']],
                    [pattern: '/**/favicon.ico',        access: ['permitAll']],
                    [pattern: '/login/**',              access: ['permitAll']], // <5>
                    [pattern: '/logout',                access: ['permitAll']],
                    [pattern: '/logout/**',             access: ['permitAll']],            
                    [pattern: '/announcement',          access: ['ROLE_BOSS', 'ROLE_EMPLOYEE']],
                    [pattern: '/announcement/index',    access: ['ROLE_BOSS', 'ROLE_EMPLOYEE']],  // <6>
                    [pattern: '/announcement/create',   access: ['ROLE_BOSS']],
                    [pattern: '/announcement/save',     access: ['ROLE_BOSS']],
                    [pattern: '/announcement/update',   access: ['ROLE_BOSS']],
                    [pattern: '/announcement/delete/*', access: ['ROLE_BOSS']],
                    [pattern: '/announcement/edit/*',   access: ['ROLE_BOSS']],            
                    [pattern: '/announcement/show/*',   access: ['ROLE_BOSS', 'ROLE_EMPLOYEE']],
                    [pattern: '/api/login',             access: ['ROLE_ANONYMOUS']], // <7>
                    [pattern: '/oauth/access_token',    access: ['ROLE_ANONYMOUS']], // <8>
                    [pattern: '/api/announcements',     access: ['ROLE_BOSS'], httpMethod: 'GET'],  // <9>
                    [pattern: '/api/announcements/*',   access: ['ROLE_BOSS'], httpMethod: 'GET'],
                    [pattern: '/api/announcements/*',   access: ['ROLE_BOSS'], httpMethod: 'DELETE'],
                    [pattern: '/api/announcements',     access: ['ROLE_BOSS'], httpMethod: 'POST'],
                    [pattern: '/api/announcements/*',   access: ['ROLE_BOSS'], httpMethod: 'PUT']
            ]
        }
    }
}

