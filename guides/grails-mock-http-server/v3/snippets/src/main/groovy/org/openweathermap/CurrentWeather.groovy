package org.openweathermap

import groovy.transform.CompileStatic

@CompileStatic
class CurrentWeather {
    Main main
    Coordinate coordinate
    List<Weather> weatherList
    Wind wind
    Sys sys
    Rain rain
    Clouds clouds
    String base
    Integer dt
    Long cityId
    String cityName
    Integer code
    Integer visibility
}
