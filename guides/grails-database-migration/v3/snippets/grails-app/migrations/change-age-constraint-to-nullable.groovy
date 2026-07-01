databaseChangeLog = {

    changeSet(author: "Nirav Assar (generated)", id: "1497551594095-1") {
        dropNotNullConstraint(columnDataType: "int", columnName: "age", tableName: "person")
    }
}
