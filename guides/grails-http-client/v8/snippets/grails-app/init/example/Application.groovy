package example

import grails.boot.GrailsApp
import grails.boot.config.GrailsAutoConfiguration
import groovy.transform.CompileStatic
import org.springframework.context.annotation.Import
import org.springframework.web.service.registry.ImportHttpServices

@CompileStatic
@ImportHttpServices(basePackages = 'example')
@Import(ItunesClientConfiguration)
class Application extends GrailsAutoConfiguration {
    static void main(String[] args) {
        GrailsApp.run(Application, args)
    }
}
