databaseChangeLog = {

    changeSet(author: "behl (generated)", id: "1687424290668-1") {
        createTable(tableName: "address") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "addressPK")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "person_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "street_name", type: "VARCHAR(255)")

            column(name: "city", type: "VARCHAR(255)")

            column(name: "zip_code", type: "VARCHAR(255)")
        }
    }

    changeSet(author: "behl (generated)", id: "1687424290668-2") {
        addForeignKeyConstraint(baseColumnNames: "person_id", baseTableName: "address", constraintName: "FK81ihijcn1kdfwffke0c0sjqeb", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "person", validate: "true")
    }

    changeSet(author: "behl (generated)", id: "1687424290668-3") {
        sql("""insert into address (version, person_id, street_name, city, zip_code)
              select 0, id, street_name, city, zip_code from person""")
    }

    changeSet(author: "behl (generated)", id: "1687424290668-4") {
        dropColumn(columnName: "city", tableName: "person")
    }

    changeSet(author: "behl (generated)", id: "1687424290668-5") {
        dropColumn(columnName: "street_name", tableName: "person")
    }

    changeSet(author: "behl (generated)", id: "1687424290668-6") {
        dropColumn(columnName: "zip_code", tableName: "person")
    }

}
