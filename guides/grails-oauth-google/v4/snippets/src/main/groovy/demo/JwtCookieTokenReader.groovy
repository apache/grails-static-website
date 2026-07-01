package demo

import grails.plugin.springsecurity.rest.token.AccessToken
import grails.plugin.springsecurity.rest.token.reader.TokenReader
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletRequest

@Slf4j
@CompileStatic
class JwtCookieTokenReader implements TokenReader {

    final static String DEFAULT_COOKIE_NAME = 'JWT'

    String cookieName = DEFAULT_COOKIE_NAME

    @Override
    AccessToken findToken(HttpServletRequest request) {

        log.debug "Looking for jwt token in a cookie named {}", cookieName
        String tokenValue = null
        Cookie cookie = request.getCookies()?.find { Cookie cookie -> cookie.name.equalsIgnoreCase(cookieName) }

        if ( cookie ) {
            tokenValue = cookie.value
        }

        log.debug "Token: ${tokenValue}"
        return tokenValue ? new AccessToken(tokenValue) : null

    }
}
