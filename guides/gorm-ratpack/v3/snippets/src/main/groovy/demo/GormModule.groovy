package demo

import com.google.inject.AbstractModule
import com.google.inject.Provides
import groovy.transform.CompileStatic
import org.grails.orm.hibernate.HibernateDatastore

@CompileStatic
class GormModule extends AbstractModule {

    @Override
    protected void configure() {}

    @Provides
    HibernateDatastore hibernateDatastore() {
        Map<String, Object> configuration = [
            'hibernate.hbm2ddl.auto':'create-drop',
            'dataSource.url':'jdbc:h2:mem:myDB'
        ] as Map<String, Object>

        new HibernateDatastore(configuration, getClass().getPackage())
    }
}
