databaseChangeLog = {

    changeSet(author: "behl (generated)", id: "1687423992173-1") {
        dropNotNullConstraint(columnDataType: "int", columnName: "age", tableName: "person")
    }
}
