package demo

import grails.config.Config
import grails.core.support.GrailsConfigurationAware
import groovy.transform.CompileStatic

@CompileStatic
class CountryService implements GrailsConfigurationAware {

    List<Country> countries = [] as List<Country>

    @Override
    void setConfiguration(Config co) {
        List<Map> l = co.getProperty('eu.countries', List)
        for ( Map m : l ) {
            countries << new Country(name: m.get('name') as String, code: m.get('code') as String)
        }
    }

    List<Country> findAll() {
        countries
    }


}