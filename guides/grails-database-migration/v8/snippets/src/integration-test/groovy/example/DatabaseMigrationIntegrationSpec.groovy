package example

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import groovy.sql.Sql
import org.springframework.beans.factory.annotation.Autowired
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName
import spock.lang.Shared
import spock.lang.Specification

import javax.sql.DataSource
import java.sql.Connection
import java.sql.DriverManager

@Integration
class DatabaseMigrationIntegrationSpec extends Specification {

    @Shared
    static PostgreSQLContainer postgres = new PostgreSQLContainer<>(DockerImageName.parse('postgres:16-alpine'))
            .withDatabaseName('migrate_path')
            .withUsername('test')
            .withPassword('test')

    @Autowired
    DataSource dataSource

    def setupSpec() {
        postgres.start()
    }

    def cleanupSpec() {
        if (postgres.running) {
            postgres.stop()
        }
    }

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

    void 'legacy person address columns migrate into address table'() {
        given: 'an isolated DB at the add-address-fields stage'
        Connection conn = DriverManager.getConnection(
                postgres.jdbcUrl,
                postgres.username,
                postgres.password
        )
        Sql sql = new Sql(conn)
        applyThroughAddressFields(sql)

        when: 'a legacy person row still has denormalized address columns'
        sql.executeInsert('''insert into person (version, name, age, street_name, city, zip_code)
                            values (0, 'Legacy Person', 42, 'Congress Ave', 'Austin', '78701')''')

        and: 'the redesign SQL from create-address-table.groovy runs'
        applyAddressRedesign(sql)

        then: 'address row matches the legacy values and person address columns are gone'
        def address = sql.firstRow('select street_name, city, zip_code from address')
        address.street_name == 'Congress Ave'
        address.city == 'Austin'
        address.zip_code == '78701'
        !columnNames(conn, 'person').contains('street_name')
        !columnNames(conn, 'person').contains('city')
        !columnNames(conn, 'person').contains('zip_code')

        cleanup:
        sql?.close()
        conn?.close()
    }

    private static void applyThroughAddressFields(Sql sql) {
        sql.execute('''
            create table person (
                id bigserial primary key,
                version bigint not null,
                name varchar(255) not null,
                age integer,
                city varchar(255),
                street_name varchar(255),
                zip_code varchar(255)
            )
        ''')
    }

    private static void applyAddressRedesign(Sql sql) {
        sql.execute('''
            create table address (
                id bigserial primary key,
                version bigint not null,
                person_id bigint not null references person(id),
                street_name varchar(255),
                city varchar(255),
                zip_code varchar(255)
            )
        ''')
        sql.execute('''
            insert into address (version, person_id, street_name, city, zip_code)
            select 0, id, street_name, city, zip_code from person
            where street_name is not null or city is not null or zip_code is not null
        ''')
        sql.execute('alter table person drop column city')
        sql.execute('alter table person drop column street_name')
        sql.execute('alter table person drop column zip_code')
    }

    private boolean tableExists(String table) {
        def conn = dataSource.connection
        try {
            return tableExists(conn, table)
        } finally {
            conn.close()
        }
    }

    private static boolean tableExists(Connection conn, String table) {
        def rs = conn.metaData.getTables(null, 'public', table, ['TABLE'] as String[])
        return rs.next()
    }

    private Set<String> columnNames(String table) {
        def conn = dataSource.connection
        try {
            return columnNames(conn, table)
        } finally {
            conn.close()
        }
    }

    private static Set<String> columnNames(Connection conn, String table) {
        def rs = conn.metaData.getColumns(null, 'public', table, null)
        def names = [] as Set
        while (rs.next()) {
            names << rs.getString('COLUMN_NAME').toLowerCase()
        }
        return names
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
