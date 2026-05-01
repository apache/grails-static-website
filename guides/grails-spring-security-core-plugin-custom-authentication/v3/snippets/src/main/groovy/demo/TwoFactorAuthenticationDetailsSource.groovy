package demo

import groovy.transform.CompileStatic
import org.springframework.security.web.authentication.WebAuthenticationDetails
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource

import javax.servlet.http.HttpServletRequest

@CompileStatic
class TwoFactorAuthenticationDetailsSource extends WebAuthenticationDetailsSource {

    @Override
    WebAuthenticationDetails buildDetails(HttpServletRequest context) {
        def details = new TwoFactorAuthenticationDetails(context)

        String position = obtainCoordinatePosition(context)
        details.coordinatePosition = position

        String value = obtainCoordinateValue(context)
        details.coordinateValue = value
        details
    }

    /**
     * Get the Coordinate Position from the request.
     * @param request
     * @return
     */
    private static String obtainCoordinatePosition(HttpServletRequest request) {
        return request.getParameter('coordinatePosition')
    }

    /**
     * Get the Coordinate Value from the request.
     * @param request
     * @return
     */
    private static String obtainCoordinateValue(HttpServletRequest request) {
        return request.getParameter('coordinateValue')
    }
}
