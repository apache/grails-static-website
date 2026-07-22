databaseChangeLog = {

    changeSet(author: 'guide (generated)', id: 'create-address-table-1') {
        preConditions(onFail: 'HALT') {
            tableExists(tableName: 'person')
            not {
                tableExists(tableName: 'address')
            }
        }
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
        preConditions(onFail: 'HALT') {
            tableExists(tableName: 'address')
            tableExists(tableName: 'person')
        }
        addForeignKeyConstraint(
            baseColumnNames: 'person_id',
            baseTableName: 'address',
            constraintName: 'fk_address_person',
            referencedColumnNames: 'id',
            referencedTableName: 'person'
        )
    }

    changeSet(author: 'guide (generated)', id: 'migrate-address-data-1') {
        preConditions(onFail: 'HALT') {
            tableExists(tableName: 'address')
            columnExists(tableName: 'person', columnName: 'street_name')
            columnExists(tableName: 'person', columnName: 'city')
            columnExists(tableName: 'person', columnName: 'zip_code')
        }
        sql('''insert into address (version, person_id, street_name, city, zip_code)
              select 0, id, street_name, city, zip_code from person
              where street_name is not null or city is not null or zip_code is not null''')
        rollback {
            sql('delete from address')
        }
    }

    changeSet(author: 'guide (generated)', id: 'drop-person-city-1') {
        preConditions(onFail: 'HALT') {
            tableExists(tableName: 'address')
            columnExists(tableName: 'person', columnName: 'city')
        }
        dropColumn(columnName: 'city', tableName: 'person')
        rollback {
            addColumn(tableName: 'person') {
                column(name: 'city', type: 'VARCHAR(255)')
            }
            sql('''update person p set city = a.city from address a where a.person_id = p.id''')
        }
    }

    changeSet(author: 'guide (generated)', id: 'drop-person-street-1') {
        preConditions(onFail: 'HALT') {
            tableExists(tableName: 'address')
            columnExists(tableName: 'person', columnName: 'street_name')
        }
        dropColumn(columnName: 'street_name', tableName: 'person')
        rollback {
            addColumn(tableName: 'person') {
                column(name: 'street_name', type: 'VARCHAR(255)')
            }
            sql('''update person p set street_name = a.street_name from address a where a.person_id = p.id''')
        }
    }

    changeSet(author: 'guide (generated)', id: 'drop-person-zip-1') {
        preConditions(onFail: 'HALT') {
            tableExists(tableName: 'address')
            columnExists(tableName: 'person', columnName: 'zip_code')
        }
        dropColumn(columnName: 'zip_code', tableName: 'person')
        rollback {
            addColumn(tableName: 'person') {
                column(name: 'zip_code', type: 'VARCHAR(255)')
            }
            sql('''update person p set zip_code = a.zip_code from address a where a.person_id = p.id''')
        }
    }
}
