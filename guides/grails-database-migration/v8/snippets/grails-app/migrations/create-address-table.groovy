databaseChangeLog = {

    changeSet(author: 'guide (generated)', id: 'create-address-table-1') {
        createTable(tableName: 'address') {
            column(autoIncrement: true, name: 'id', type: 'BIGINT') {
                constraints(nullable: false, primaryKey: true, primaryKeyName: 'addressPK')
            }
            column(name: 'version', type: 'BIGINT') {
                constraints(nullable: false)
            }
            column(name: 'person_id', type: 'BIGINT') {
                constraints(nullable: false)
            }
            column(name: 'street_name', type: 'VARCHAR(255)')
            column(name: 'city', type: 'VARCHAR(255)')
            column(name: 'zip_code', type: 'VARCHAR(255)')
        }
    }

    changeSet(author: 'guide (generated)', id: 'create-address-fk-1') {
        addForeignKeyConstraint(
            baseColumnNames: 'person_id',
            baseTableName: 'address',
            constraintName: 'fk_address_person',
            referencedColumnNames: 'id',
            referencedTableName: 'person'
        )
    }

    changeSet(author: 'guide (generated)', id: 'migrate-address-data-1') {
        sql('''insert into address (version, person_id, street_name, city, zip_code)
              select 0, id, street_name, city, zip_code from person
              where street_name is not null or city is not null or zip_code is not null''')
    }

    changeSet(author: 'guide (generated)', id: 'drop-person-city-1') {
        dropColumn(columnName: 'city', tableName: 'person')
    }

    changeSet(author: 'guide (generated)', id: 'drop-person-street-1') {
        dropColumn(columnName: 'street_name', tableName: 'person')
    }

    changeSet(author: 'guide (generated)', id: 'drop-person-zip-1') {
        dropColumn(columnName: 'zip_code', tableName: 'person')
    }
}
