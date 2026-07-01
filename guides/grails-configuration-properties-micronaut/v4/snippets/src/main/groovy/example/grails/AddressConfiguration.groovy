package example.grails

import io.micronaut.context.annotation.ConfigurationProperties

@ConfigurationProperties("address") // <1>
class AddressConfiguration {
    String street
    String city
    String country
}
