package demo

import grails.testing.mixin.integration.Integration
import org.grails.datastore.mapping.multitenancy.exceptions.TenantNotFoundException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletWebRequest
import spock.lang.Specification
import spock.lang.IgnoreIf

@IgnoreIf( { System.getenv('TRAVIS') as boolean } )
@Integration
class CurrentUserByJwtTenantResolverSpec extends Specification {

    @Autowired
    CurrentUserByJwtTenantResolver currentUserTenantResolver

    void "Test HttpHeader resolver throws an exception outside a web request"() {
        when:
        currentUserTenantResolver.resolveTenantIdentifier()

        then:
        def e = thrown(TenantNotFoundException)
        e.message == "Tenant could not be resolved outside a web request"
    }


    void "Test not tenant id found"() {
        setup:
        def request = new MockHttpServletRequest("GET", "/foo")
        RequestContextHolder.setRequestAttributes(new ServletWebRequest(request))

        when:
        currentUserTenantResolver.resolveTenantIdentifier()

        then:
        def e = thrown(TenantNotFoundException)
        e.message == "Tenant could not be resolved from HTTP Header: ${CurrentUserByJwtTenantResolver.HEADER_NAME}"

        cleanup:
        RequestContextHolder.setRequestAttributes(null)
    }

    void "Test HttpHeader value is the tenant id when a request is present"() {

        setup:
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/foo")
        String jwt = '''\
eyJhbGciOiJIUzI1NiJ9.eyJwcmluY2lwYWwiOiJINHNJQUFBQUFBQUFBSlZTdjA4VVFSUitleHhDZ2xF\
d2tjUUNHN0V6ZXdtV1Z3RkJBcGtjeHZNc01OSE03VDZXZ2RtWmRXYjJ1R3ZJVlZKUVFGUVNFbHBMXC9oT\
nRcL0FPTUZMVFV0THhaT1BhMElVNjFlZlB0OSt2TjZRV01XZ012RThPRnRHRW04MFNvMEdaR3FNUmlsQn\
ZoZW1GdTBjVG9Dc1J5QVd6UkJLNVBVSUdBUVVYRURoNnhMZDdoTmNsVlVsdHJiMkhrNmwwRGM5b2tONHd\
iaHFlNG84MTJlTXNkYVlOXC9DWlRVd2ZjS2pLM0RGSThpblN2WDBHcXBtd21EOFRwTWxqT21vMjBcL2Vo\
elJEU29udUxURDBERlV2QzB4WmpEQmM3ZXBTVldnZGZEdzJtenVoS3cxMGRVWmpHZmNXbkwzVDVLbTg5Yj\
l2YmVwS01FbjJJVnFOd3ZvVUhmUFBUVDBQT0dpbHBKU0M2M3NiRXVsT2hZYndvc1RmM1wvbXk2K0RrMzZy\
QWtDZHZMajduM0wrWkFINlB6NWNQaTJLRGlJSDAwUFdTMWk5bTVHYnFaTDVyVUd2XC9QdjQ5ZGVqaTczM0\
k2VHNFYVwvK2Z4K3o4emZOOVJaMW1uSERuUjdhRWRIdVZQMDNrU1wvY1RUN1lRaTlzaWpTVFNDOUtPWXh2\
SlVwaWlsczFXZzc2ZG5EXC96UnBiK3ZodWhiSDVsWVlmM090UWRtMUkrRUdSMnk4c1pKcld0WDkrK1BQZ\
zJSOGlXWVhSRHBjNVV1MlRKYWlScDIwMG4wK1BaaWErbmUwWElRWVArZ3JPZUdRVkZBTUFBQT09Iiwic3\
ViIjoidmVjdG9yIiwicm9sZXMiOlsiUk9MRV9WSUxMQUlOIl0sImlhdCI6MTUwNDc4MTEyNX0.cf5rGNrNolchQ3QyMsPB544fwzYGiihBkRF8KU6soxc'''

        request.addHeader('Authorization', "Bearer $jwt")
        RequestContextHolder.setRequestAttributes(new ServletWebRequest(request))


        when:
        def tenantId = currentUserTenantResolver.resolveTenantIdentifier()

        then:
        tenantId == "vector"

        cleanup:
        RequestContextHolder.setRequestAttributes(null)
    }
}

