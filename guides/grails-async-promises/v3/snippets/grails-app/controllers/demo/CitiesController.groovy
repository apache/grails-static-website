package demo

import grails.async.Promise
import static grails.async.Promises.*
import groovy.transform.CompileStatic
import org.openweathermap.CurrentWeather
import org.openweathermap.OpenweathermapService
import org.openweathermap.Unit

@CompileStatic
class CitiesController {
    
    OpenweathermapService openweathermapService
    
    def index(String unit, boolean async) {
        Unit unitEnum = Unit.unitWithString(unit)

        if ( async ) { // <1>
            Promise<List<CurrentWeather>> currentWeatherList = openweathermapService.findCurrentWeatherByCitiesAndCountryCodeWithPromises(LargestUSCities.CITIES, 'us', unitEnum)
            return tasks( // <2>
                    currentWeatherList: currentWeatherList,
                    unit: createBoundPromise(unitEnum)
            )
        } else { // <3>
            List<CurrentWeather> currentWeatherList = openweathermapService.findCurrentWeatherByCitiesAndCountryCode(LargestUSCities.CITIES, 'us', unitEnum)
            return [currentWeatherList: currentWeatherList, unit: unitEnum]
        }        

    }
}