databaseChangeLog = {

    include file: 'create-person-table.groovy'
    include file: 'change-age-constraint-to-nullable.groovy'
    include file: 'add-address-fields-to-person.groovy'
    include file: 'create-address-table.groovy'
}
