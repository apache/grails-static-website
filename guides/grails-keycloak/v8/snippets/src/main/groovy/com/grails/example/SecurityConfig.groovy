package com.grails.example

import groovy.util.logging.Slf4j

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.web.SecurityFilterChain

@Configuration
@Slf4j
@EnableMethodSecurity
class SecurityConfig {

    @Autowired
    KeycloakAuthoritiesMapper keycloakAuthoritiesMapper

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http,
                                            ClientRegistrationRepository clientRegistrationRepository) throws Exception {
        def oidcLogoutSuccessHandler = new OidcClientInitiatedLogoutSuccessHandler(clientRegistrationRepository)

        oidcLogoutSuccessHandler.setPostLogoutRedirectUri("{baseUrl}")
        http
                .authorizeHttpRequests { auth ->
                    auth
                            .requestMatchers(
                                    "/",
                                    "/home/**",
                                    "/logout/**",
                                    "/error",
                                    "/assets/**",
                                    "/images/**",
                                    "/css/**",
                                    "/js/**",
                                    "/404",
                                    "/403"
                            ).permitAll()
                            .anyRequest().authenticated()
                }
                .oauth2Login { oauth ->
                    oauth.userInfoEndpoint { userInfo ->
                        userInfo.userAuthoritiesMapper(keycloakAuthoritiesMapper)
                    }
                }
                .logout { logout ->
                    logout.logoutSuccessHandler(oidcLogoutSuccessHandler)
                }
        return http.build()
    }
}
