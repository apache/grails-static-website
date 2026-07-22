databaseChangeLog = {

    changeSet(author: 'guide (generated)', id: 'create-person-table-1') {
        createTable(tableName: 'person') {
            column(autoIncrement: true, name: 'id', type: 'BIGINT') {
                constraints(nullable: false, primaryKey: true, primaryKeyName: 'personPK')
            }
            column(name: 'version', type: 'BIGINT') {
                constraints(nullable: false)
            }
            column(name: 'age', type: 'INTEGER') {
                constraints(nullable: false)
            }
            column(name: 'name', type: 'VARCHAR(255)') {
                constraints(nullable: false)
            }
        }
    }
}
