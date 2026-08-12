package example.grails.jobrunr

import javax.sql.DataSource
import org.jobrunr.JobRunrException
import org.jobrunr.spring.autoconfigure.JobRunrProperties
import org.springframework.jdbc.datasource.DriverManagerDataSource
import spock.lang.Specification

class JobRunrStorageConfigSpec extends Specification {
    void 'storage configuration applies the JobRunr database properties'() {
        given:
        DataSource dataSource = new DriverManagerDataSource(
            "jdbc:h2:mem:jobrunr-storage-config-${System.nanoTime()};DB_CLOSE_DELAY=-1",
            'sa',
            ''
        )
        JobRunrProperties properties = new JobRunrProperties()
        properties.database.tablePrefix = 'GUIDE_'
        properties.database.skipCreate = false

        when:
        new JobRunrStorageConfig().storageProvider(dataSource, properties)

        then:
        dataSource.connection.withCloseable { connection ->
            connection.metaData.getTables(null, null, 'GUIDE%', null).next()
        }

        when:
        DataSource skipCreateDataSource = new DriverManagerDataSource(
            "jdbc:h2:mem:jobrunr-storage-config-skip-${System.nanoTime()};DB_CLOSE_DELAY=-1",
            'sa',
            ''
        )
        properties.database.skipCreate = true
        properties.database.tablePrefix = 'SKIP_'
        new JobRunrStorageConfig().storageProvider(skipCreateDataSource, properties)

        then:
        thrown(JobRunrException)
        !skipCreateDataSource.connection.withCloseable { connection ->
            connection.metaData.getTables(null, null, 'SKIP%', null).next()
        }
    }
}
