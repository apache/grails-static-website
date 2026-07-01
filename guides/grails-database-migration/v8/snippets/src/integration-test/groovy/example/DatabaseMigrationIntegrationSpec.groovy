package example

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification

import javax.sql.DataSource

@Integration
class DatabaseMigrationIntegrationSpec extends Specification {

    @Autowired
    DataSource dataSource

    void 'Liquibase tracking tables exist on a clean database'() {
        expect:
        tableExists('databasechangelog')
        tableExists('databasechangeloglock')
    }

    void 'person table exists with expected columns and no address columns'() {
        expect:
        tableExists('person')
        columnNames('person').containsAll(['id', 'version', 'name', 'age'])
        !columnNames('person').contains('street_name')
        !columnNames('person').contains('city')
        !columnNames('person').contains('zip_code')
    }

    void 'address table exists with person_id foreign key'() {
        expect:
        tableExists('address')
        columnNames('address').containsAll(['id', 'version', 'person_id', 'street_name', 'city', 'zip_code'])
        foreignKeyExists('address', 'person_id', 'person', 'id')
    }

    @Rollback
    void 'GORM can save Person and Address after migrations apply'() {
        when:
        def person = Person.withTransaction {
            def p = new Person(name: 'Test Person', age: 30).save(flush: true, failOnError: true)
            new Address(
                person: p,
                streetName: 'Main St',
                city: 'Austin',
                zipCode: '78701'
            ).save(flush: true, failOnError: true)
            p
        }

        then:
        person.id
        Address.countByPerson(person) == 1
        Address.findByPerson(person).city == 'Austin'
    }

    private boolean tableExists(String table) {
        def conn = dataSource.connection
        try {
            def rs = conn.metaData.getTables(null, 'public', table, ['TABLE'] as String[])
            return rs.next()
        } finally {
            conn.close()
        }
    }

    private Set<String> columnNames(String table) {
        def conn = dataSource.connection
        try {
            def rs = conn.metaData.getColumns(null, 'public', table, null)
            def names = [] as Set
            while (rs.next()) {
                names << rs.getString('COLUMN_NAME').toLowerCase()
            }
            return names
        } finally {
            conn.close()
        }
    }

    private boolean foreignKeyExists(String fkTable, String fkColumn, String pkTable, String pkColumn) {
        def conn = dataSource.connection
        try {
            def rs = conn.metaData.getImportedKeys(null, 'public', fkTable)
            while (rs.next()) {
                if (rs.getString('FKTABLE_NAME').equalsIgnoreCase(fkTable) &&
                    rs.getString('FKCOLUMN_NAME').equalsIgnoreCase(fkColumn) &&
                    rs.getString('PKTABLE_NAME').equalsIgnoreCase(pkTable) &&
                    rs.getString('PKCOLUMN_NAME').equalsIgnoreCase(pkColumn)) {
                    return true
                }
            }
            return false
        } finally {
            conn.close()
        }
    }
}
