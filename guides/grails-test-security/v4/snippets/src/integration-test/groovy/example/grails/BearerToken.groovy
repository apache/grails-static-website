package example.grails

import com.fasterxml.jackson.annotation.JsonProperty

class BearerToken {
    @JsonProperty('access_token')
    String accessToken

    @JsonProperty('refresh_token')
    String refreshToken

    List<String> roles

    String username
}
