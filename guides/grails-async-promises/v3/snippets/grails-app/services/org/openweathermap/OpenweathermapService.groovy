//tag::packageAndImports[]
package org.openweathermap

import grails.async.Promise
import grails.async.PromiseList
import grails.plugins.rest.client.RestBuilder
import grails.plugins.rest.client.RestResponse
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.springframework.beans.factory.annotation.Autowired

//end::packageAndImports[]
//tag::promisesImports[]
import static grails.async.Promises.*
//end::promisesImports[]

//tag::grailsConfigurationAware[]
@CompileStatic
class OpenweathermapService {

    @Autowired OpenweathermapConfiguration openweathermapConfiguration
//end::grailsConfigurationAware[]

//tag::currentWeather[]
    @CompileDynamic
    CurrentWeather currentWeather(Unit units = Unit.Standard) {
        currentWeather(openweathermapConfiguration.cityName, openweathermapConfiguration.countryCode, units)
    }


    @CompileDynamic
    CurrentWeather currentWeather(String cityName, String countryCode, Unit unit = Unit.Standard) {
        RestBuilder rest = new RestBuilder()
        String url = "${openweathermapConfiguration.openWeatherUrl}/data/2.5/weather?q={city},{countryCode}&appid={appid}"
        Map params = [city: cityName, countryCode: countryCode, appid: openweathermapConfiguration.appid]
        String unitParam = unitParameter(unit)
        if ( unitParam ) {
            params.units = unitParam
            url += "&units={units}"
        }        
        RestResponse restResponse = rest.get(url) { // <1>
            urlVariables params
        }

        if ( restResponse.statusCode.value() == 200 && restResponse.json ) {
            return OpenweathermapParser.currentWeatherFromJSONElement(restResponse.json) // <2>
        }
        null // <3>
    }

  /**
    * @return null if Standard Unit
    */
    String unitParameter(Unit unit)  {
        switch ( unit ) {
            case Unit.Metric:
                return 'metric'
            case Unit.Imperial:
                return 'imperial'                
            default:
                return null
        }
    }
//end::currentWeather[]

//tag::findCurrentWeatherByCitiesAndCountryCodeAsync[]
    Promise<List<CurrentWeather>> findCurrentWeatherByCitiesAndCountryCodeWithPromises(List<String> cities, String countryCode, Unit unit) {
        PromiseList<CurrentWeather> list = new PromiseList<CurrentWeather>()
        cities.each { String city -> 
            list << task { // <1>
                currentWeather(city, countryCode, unit)
            }
        }
        return list // <2>
    }
//end::findCurrentWeatherByCitiesAndCountryCodeAsync[]
//tag::findCurrentWeatherByCitiesAndCountryCodeSynchronous[]    
    List<CurrentWeather> findCurrentWeatherByCitiesAndCountryCode(List<String> cities, String countryCode, Unit unit) {
        cities.collect { currentWeather(it, countryCode, unit) }    
    }
//end::findCurrentWeatherByCitiesAndCountryCodeSynchronous[]    
}
