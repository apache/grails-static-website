package demo

import groovy.transform.CompileStatic
import org.openweathermap.CurrentWeather
import org.openweathermap.OpenweathermapService
import org.openweathermap.Unit

@CompileStatic
class HomeController {
    OpenweathermapService openweathermapService

    def index(String unit) {
        Unit unitEnum = Unit.unitWithString(unit)
        CurrentWeather currentWeather = openweathermapService.currentWeather(unitEnum)
        [currentWeather: currentWeather, unit: unitEnum]
    }
}
