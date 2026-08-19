package example.grails.jobrunr

import javax.sql.DataSource
import org.jobrunr.jobs.mappers.JobMapper
import org.jobrunr.spring.autoconfigure.JobRunrProperties
import org.jobrunr.storage.StorageProvider
import org.jobrunr.storage.StorageProviderUtils.DatabaseOptions
import org.jobrunr.storage.sql.common.SqlStorageProviderFactory
import org.jobrunr.utils.mapper.jackson.JacksonJsonMapper
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class JobRunrStorageConfig {
    @Bean
    StorageProvider storageProvider(
        @Qualifier('dataSource_jobrunr') DataSource jobrunrDataSource,
        JobRunrProperties jobRunrProperties
    ) {
        StorageProvider storageProvider = SqlStorageProviderFactory.using(
            jobrunrDataSource,
            jobRunrProperties.database.tablePrefix,
            jobRunrProperties.database.skipCreate ? DatabaseOptions.SKIP_CREATE : DatabaseOptions.CREATE
        )
        storageProvider.setJobMapper(new JobMapper(new JacksonJsonMapper()))
        storageProvider
    }
}
