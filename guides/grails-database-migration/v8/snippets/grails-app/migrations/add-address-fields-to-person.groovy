databaseChangeLog = {

    changeSet(author: 'guide (generated)', id: 'add-address-city-1') {
        addColumn(tableName: 'person') {
            column(name: 'city', type: 'VARCHAR(255)')
        }
    }

    changeSet(author: 'guide (generated)', id: 'add-address-street-1') {
        addColumn(tableName: 'person') {
            column(name: 'street_name', type: 'VARCHAR(255)')
        }
    }

    changeSet(author: 'guide (generated)', id: 'add-address-zip-1') {
        addColumn(tableName: 'person') {
            column(name: 'zip_code', type: 'VARCHAR(255)')
        }
    }
}
