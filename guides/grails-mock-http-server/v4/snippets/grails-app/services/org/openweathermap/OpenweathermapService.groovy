//tag::packageAndImports[]
package org.openweathermap

import grails.config.Config
import grails.core.support.GrailsConfigurationAware
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.BlockingHttpClient
import io.micronaut.http.client.HttpClient
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.http.uri.UriBuilder
import org.grails.web.json.JSONObject


//end::packageAndImports[]
//tag::promisesImports[]
import static grails.async.Promises.*
//end::promisesImports[]

//tag::grailsConfigurationAware[]
@CompileStatic
class OpenweathermapService implements GrailsConfigurationAware {
    String appid
    String cityName
    String countryCode
    BlockingHttpClient client

    @Override
    void setConfiguration(Config co) {
        setupHttpClient(co.getProperty('openweather.url', String, 'http://api.openweathermap.org'))
        appid = co.getProperty('openweather.appid', String)
        cityName = co.getProperty('openweather.cityName', String)
        countryCode = co.getProperty('openweather.countryCode', String)
    }

    void setupHttpClient(String url) {
        this.client = HttpClient.create(url.toURL()).toBlocking()
    }
//end::grailsConfigurationAware[]

//tag::currentWeather[]
    CurrentWeather currentWeather(Unit units = Unit.Standard) {
        currentWeather(cityName, countryCode, units)
    }

    CurrentWeather currentWeather(String cityName, String countryCode, Unit unit = Unit.Standard) {
        try {
            HttpRequest request = HttpRequest.GET(currentWeatherUri(cityName, countryCode, unit))
            return client.retrieve(request, CurrentWeather)

        } catch (HttpClientResponseException e) {
            return null // <3>
        }
    }

    URI currentWeatherUri(String cityName, String countryCode, Unit unit = Unit.Standard) {
        UriBuilder uriBuilder = UriBuilder.of('/data/2.5/weather')
                .queryParam('q', "${cityName},${countryCode}".toString())
                .queryParam('appid', appid)
        String unitParam = unitParameter(unit)
        if (unitParam) {
            uriBuilder = uriBuilder.queryParam('units', unitParam)
        }
        uriBuilder.build()
    }

    String unitParameter(Unit unit)  {
        unit == Unit.Standard ? null : unit?.toString()
    }
//end::currentWeather[]
}
