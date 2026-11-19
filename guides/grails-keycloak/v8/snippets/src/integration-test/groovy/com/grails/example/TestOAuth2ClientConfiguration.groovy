package com.grails.example

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository
import org.springframework.security.oauth2.core.AuthorizationGrantType

/**
 * Offline stand-in for the Keycloak client registration.
 *
 * The application's issuer-uri makes Spring Boot call Keycloak's
 * /.well-known/openid-configuration endpoint while building the
 * clientRegistrationRepository bean. That HTTP call would make every
 * @Integration spec fail whenever Keycloak is not running, so declaring the
 * bean here makes the auto-configuration back off instead.
 */
@TestConfiguration(proxyBeanMethods = false)
class TestOAuth2ClientConfiguration {

    @Bean
    ClientRegistrationRepository clientRegistrationRepository() {
        def registration = ClientRegistration
                .withRegistrationId('keycloak')
                .clientId('test-client')
                .clientSecret('test-secret')
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri('{baseUrl}/login/oauth2/code/{registrationId}')
                .scope('openid', 'profile', 'email')
                .authorizationUri('http://localhost/oauth2/authorize')
                .tokenUri('http://localhost/oauth2/token')
                .userInfoUri('http://localhost/oauth2/userinfo')
                .userNameAttributeName('preferred_username')
                .jwkSetUri('http://localhost/oauth2/jwks')
                .clientName('Keycloak')
                // SecurityConfig's logout handler reads this key when building its redirect
                .providerConfigurationMetadata([
                        end_session_endpoint: 'http://localhost/oauth2/logout'
                ])
                .build()

        new InMemoryClientRegistrationRepository(registration)
    }
}
