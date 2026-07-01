package org.openweathermap

class OpenweathermapTagLib {
    static namespace = "openweather"

    def image = { attrs ->
        out << "<img src=\"http://openweathermap.org/img/w/${attrs.icon}.png\"/>"
    }

    def temperatureSymbol = { attrs ->
        if ( attrs.unit == Unit.Imperial ) {
            out << '°F'
        } else if ( attrs.unit == Unit.Metric ) {
            out << '°C'
        }

    }
}