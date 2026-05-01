databaseChangeLog = {

    changeSet(author: "behl (generated)", id: "1687424134696-1") {
        addColumn(tableName: "person") {
            column(name: "city", type: "varchar(255)")
        }
    }

    changeSet(author: "behl (generated)", id: "1687424134696-2") {
        addColumn(tableName: "person") {
            column(name: "street_name", type: "varchar(255)")
        }
    }

    changeSet(author: "behl (generated)", id: "1687424134696-3") {
        addColumn(tableName: "person") {
            column(name: "zip_code", type: "varchar(255)")
        }
    }
}
