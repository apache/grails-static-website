package org.openweathermap

import groovy.transform.CompileStatic

@CompileStatic
class Sys {
    Long id
    String type
    String message
    String country
    Integer sunrise
    Integer sunset
}
