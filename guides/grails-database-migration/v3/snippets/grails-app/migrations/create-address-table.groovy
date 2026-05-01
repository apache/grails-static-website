databaseChangeLog = {

    // tag::createAddressTable[]
    changeSet(author: "Nirav Assar (generated)", id: "1497553930799-1") {
        createTable(tableName: "address") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(primaryKey: "true", primaryKeyName: "addressPK")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "city", type: "VARCHAR(255)")

            column(name: "person_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "street_name", type: "VARCHAR(255)")

            column(name: "zip_code", type: "VARCHAR(255)")
        }
    }
    // end::createAddressTable[]

    // tag::foreignKey[] 
    changeSet(author: "Nirav Assar (generated)", id: "1497553930799-2") {
        addForeignKeyConstraint(baseColumnNames: "person_id", baseTableName: "address", constraintName: "FK81ihijcn1kdfwffke0c0sjqeb", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "person")
    }
    // end::foreignKey[]

    // tag::customDataSql[]
    // <1>
    changeSet(author: "a488338 (generated)", id: "migrate-person-data") {
        sql("""insert into address (version, person_id, street_name, city, zip_code)
              select 0, id, street_name, city, zip_code from person""")
    }
    // end::customDataSql[]

    // tag::dropColumns[]
    changeSet(author: "Nirav Assar (generated)", id: "1497553930799-3") {
        dropColumn(columnName: "city", tableName: "person")
    }

    changeSet(author: "Nirav Assar (generated)", id: "1497553930799-4") {
        dropColumn(columnName: "street_name", tableName: "person")
    }

    changeSet(author: "Nirav Assar (generated)", id: "1497553930799-5") {
        dropColumn(columnName: "zip_code", tableName: "person")
    }
    // end::dropColumns[]
}
