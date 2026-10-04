package example.grails

import grails.testing.mixin.integration.Integration
import javax.sql.DataSource
import org.jobrunr.storage.StorageProvider
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.transaction.event.TransactionalEventListenerFactory
import spock.lang.Specification

@Integration
class JobRunrIntegrationSpec extends Specification {
    @Autowired
    DeliveryService deliveryService

    @Autowired
    StorageProvider storageProvider

    @Autowired
    TransactionalEventListenerFactory transactionalEventListenerFactory

    @Autowired
    @Qualifier('dataSource')
    DataSource applicationDataSource

    @Autowired
    @Qualifier('dataSource_jobrunr')
    DataSource jobrunrDataSource

    void 'JobRequest completes a GORM delivery while JobRunr owns separate tables'() {
        when:
        int existingJobs = storageProvider.jobStats.total
        Delivery delivery = deliveryService.create("delivery-${System.nanoTime()}")

        then:
        eventually { storageProvider.jobStats.total == existingJobs + 1 }
        eventually { loadDelivery(delivery.id)?.status == 'SUCCEEDED' }
        loadDelivery(delivery.id).progress == 100
        loadDelivery(delivery.id).completedAt != null
        storageProvider != null
        transactionalEventListenerFactory != null
        tableExists(jobrunrDataSource, 'JOBRUNR_JOBS')
        !tableExists(applicationDataSource, 'JOBRUNR_JOBS')
    }

    private static boolean eventually(Closure<Boolean> condition) {
        for (int attempt = 0; attempt < 120; attempt++) {
            if (condition.call()) {
                return true
            }
            Thread.sleep(250)
        }
        false
    }

    private static boolean tableExists(DataSource dataSource, String tableName) {
        dataSource.connection.withCloseable { connection ->
            connection.metaData.getTables(null, null, tableName, null).next()
        }
    }

    private static Delivery loadDelivery(Long id) {
        Delivery.withNewTransaction {
            Delivery.get(id)
        }
    }
}
