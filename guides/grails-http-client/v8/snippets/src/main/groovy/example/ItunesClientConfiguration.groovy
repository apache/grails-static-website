package example

import groovy.transform.CompileStatic
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter
import org.springframework.web.client.support.RestClientHttpServiceGroupConfigurer

@CompileStatic
@Configuration
class ItunesClientConfiguration {

    private static final MediaType JAVASCRIPT = MediaType.parseMediaType('text/javascript')

    @Bean
    RestClientHttpServiceGroupConfigurer itunesBaseUrlConfigurer(
            @Value('${itunes.base-url:https://itunes.apple.com}') String itunesBaseUrl) {
        return { groups ->
            groups.forEachClient { group, builder ->
                builder.baseUrl(itunesBaseUrl)
                builder.messageConverters { List<HttpMessageConverter<?>> converters ->
                    JacksonJsonHttpMessageConverter jacksonConverter =
                            (JacksonJsonHttpMessageConverter) converters.find { HttpMessageConverter<?> converter ->
                                converter instanceof JacksonJsonHttpMessageConverter
                            }
                    jacksonConverter.supportedMediaTypes = jacksonConverter.supportedMediaTypes + JAVASCRIPT
                }
            }
        }
    }
}
