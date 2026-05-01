databaseChangeLog = {

    changeSet(author: "Nirav Assar (generated)", id: "1497552798178-1") {
        addColumn(tableName: "person") {
            column(name: "city", type: "varchar(255)")
        }
    }

    changeSet(author: "Nirav Assar (generated)", id: "1497552798178-2") {
        addColumn(tableName: "person") {
            column(name: "street_name", type: "varchar(255)")
        }
    }

    changeSet(author: "Nirav Assar (generated)", id: "1497552798178-3") {
        addColumn(tableName: "person") {
            column(name: "zip_code", type: "varchar(255)")
        }
    }
}
