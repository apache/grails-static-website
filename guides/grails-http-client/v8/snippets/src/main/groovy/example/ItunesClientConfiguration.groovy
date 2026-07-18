package example

import groovy.transform.CompileStatic
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.support.RestClientHttpServiceGroupConfigurer

@CompileStatic
@Configuration
class ItunesClientConfiguration {

    @Bean
    RestClientHttpServiceGroupConfigurer itunesBaseUrlConfigurer(
            @Value('${itunes.base-url:https://itunes.apple.com}') String itunesBaseUrl) {
        return { groups ->
            groups.forEachClient { group, builder ->
                builder.baseUrl(itunesBaseUrl)
            }
        }
    }
}
