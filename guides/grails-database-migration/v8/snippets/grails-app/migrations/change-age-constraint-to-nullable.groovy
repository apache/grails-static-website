databaseChangeLog = {

    changeSet(author: 'guide (generated)', id: 'change-age-nullable-1') {
        dropNotNullConstraint(columnDataType: 'integer', columnName: 'age', tableName: 'person')
    }
}
