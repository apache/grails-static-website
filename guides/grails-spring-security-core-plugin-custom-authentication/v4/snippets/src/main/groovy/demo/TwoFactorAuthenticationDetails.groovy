package demo

import groovy.transform.Canonical
import groovy.transform.CompileStatic
import org.springframework.security.web.authentication.WebAuthenticationDetails

import javax.servlet.http.HttpServletRequest

@Canonical
@CompileStatic
class TwoFactorAuthenticationDetails extends WebAuthenticationDetails {
    String coordinatePosition
    String coordinateValue

    TwoFactorAuthenticationDetails(HttpServletRequest request) {
        super(request)
    }
}
