//tag::staticRules[]
grails.plugin.springsecurity.controllerAnnotations.staticRules = [
	[pattern: '/',               access: ['permitAll']],
	[pattern: '/error',          access: ['permitAll']],
	[pattern: '/index',          access: ['permitAll']],
	[pattern: '/index.gsp',      access: ['permitAll']],
	[pattern: '/shutdown',       access: ['permitAll']],
	[pattern: '/assets/**',      access: ['permitAll']],
	[pattern: '/**/js/**',       access: ['permitAll']],
	[pattern: '/**/css/**',      access: ['permitAll']],
	[pattern: '/**/images/**',   access: ['permitAll']],
	[pattern: '/**/favicon.ico', access: ['permitAll']]
]
//end::staticRules[]
//tag::grailsPluginSpringSecurityRest[]
grails {
	plugin {
		springsecurity {
			rest {
				token {
					validation {
						useBearerToken = false // <1>
						enableAnonymousAccess = true // <2>
					}
					storage {
						jwt {
							secret = 'foobar123'*4 //<3>
						}
					}
				}
				oauth {
					frontendCallbackUrl = { String tokenValue -> "http://127.0.0.1:8080/auth/success?token=${tokenValue}" } //<4>
					twitter {
						client = org.pac4j.oauth.client.TwitterClient //<5>
						key = '${TWITTER_KEY}' //<6>
						secret = '${TWITTER_SECRET}' //<7>
						defaultRoles = [] //<8>
					}
				}
			}
			providerNames = ['anonymousAuthenticationProvider'] // <9>
		}
	}
}
//end::grailsPluginSpringSecurityRest[]

//tag::filterChain[]
String ANONYMOUS_FILTERS = 'anonymousAuthenticationFilter,restTokenValidationFilter,restExceptionTranslationFilter,filterInvocationInterceptor' // <1>
grails.plugin.springsecurity.filterChain.chainMap = [
		[pattern: '/dbconsole/**',      filters: 'none'],
		[pattern: '/assets/**',      filters: 'none'],
		[pattern: '/**/js/**',       filters: 'none'],
		[pattern: '/**/css/**',      filters: 'none'],
		[pattern: '/**/images/**',   filters: 'none'],
		[pattern: '/**/favicon.ico', filters: 'none'],
		[pattern: '/', filters: ANONYMOUS_FILTERS], // <1>
		[pattern: '/book/show/*', filters: ANONYMOUS_FILTERS],  // <1>
		[pattern: '/bookFavourite/index', filters: ANONYMOUS_FILTERS], // <1>
		[pattern: '/auth/success', filters: ANONYMOUS_FILTERS], // <1>
		[pattern: '/oauth/authenticate/twitter', filters: ANONYMOUS_FILTERS], // <1>
		[pattern: '/oauth/callback/twitter', filters: ANONYMOUS_FILTERS], // <1>
		[pattern: '/**', filters: 'JOINED_FILTERS,-anonymousAuthenticationFilter,-exceptionTranslationFilter,-authenticationProcessingFilter,-securityContextPersistenceFilter,-rememberMeAuthenticationFilter'],  // <1>
]
//end::filterChain[]

//tag::logoutHandlers[]
grails.plugin.springsecurity.logout.handlerNames = ['rememberMeServices', 'securityContextLogoutHandler', 'cookieClearingLogoutHandler']
//end::logoutHandlers[]