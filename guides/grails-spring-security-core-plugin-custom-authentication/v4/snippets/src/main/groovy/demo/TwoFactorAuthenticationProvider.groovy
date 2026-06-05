package demo

import groovy.transform.CompileStatic
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.userdetails.UserDetails

@CompileStatic
class TwoFactorAuthenticationProvider extends DaoAuthenticationProvider {

    CoordinateValidator coordinateValidator

    protected void additionalAuthenticationChecks(UserDetails userDetails,
                                                  UsernamePasswordAuthenticationToken authentication)
            throws AuthenticationException {

        super.additionalAuthenticationChecks(userDetails, authentication)

        Object details = authentication.details

        if ( !(details instanceof TwoFactorAuthenticationDetails) ) {
            logger.debug("Authentication failed: authenticationToken principal is not a TwoFactorPrincipal");
            throw new BadCredentialsException(messages.getMessage(
                    "AbstractUserDetailsAuthenticationProvider.badCredentials",
                    "Bad credentials"));
        }
        def twoFactorAuthenticationDetails = details as TwoFactorAuthenticationDetails


        if ( !coordinateValidator.isValidValueForPositionAndUserName(twoFactorAuthenticationDetails.coordinateValue, twoFactorAuthenticationDetails.coordinatePosition, authentication.name) ) {
            logger.debug("Authentication failed: coordiante note valid");
            throw new BadCredentialsException(messages.getMessage(
                    "AbstractUserDetailsAuthenticationProvider.badCredentials",
                    "Bad credentials"));
        }
    }

}
