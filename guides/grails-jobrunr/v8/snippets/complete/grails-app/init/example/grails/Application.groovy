package example.grails

import example.grails.jobrunr.JobRunrStorageConfig
import grails.boot.GrailsApp
import grails.boot.config.GrailsAutoConfiguration
import org.grails.datastore.gorm.boot.autoconfigure.HibernateGormAutoConfiguration
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Import

@Import([HibernateGormAutoConfiguration, JobRunrStorageConfig])
@ComponentScan('example.grails')
class Application extends GrailsAutoConfiguration {
    static void main(String[] args) {
        GrailsApp.run(Application, args)
    }
}
