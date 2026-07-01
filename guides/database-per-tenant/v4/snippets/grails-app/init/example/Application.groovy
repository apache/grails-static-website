package example

import grails.boot.GrailsApp
import grails.boot.config.GrailsAutoConfiguration
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import grails.gorm.transactions.Transactional
import groovy.transform.CompileStatic

@CompileStatic
class Application extends GrailsAutoConfiguration implements ApplicationRunner { // <1>

    static void main(String[] args) {
        GrailsApp.run(Application, args)
    }

    @Override
    @Transactional
    void run(ApplicationArguments args) throws Exception { // <2>
        Manufacturer.saveAll( // <3>
            new Manufacturer(name: 'Audi'),
            new Manufacturer(name: 'Ford')
        )
    }
}
