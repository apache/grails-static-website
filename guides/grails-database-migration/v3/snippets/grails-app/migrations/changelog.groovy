databaseChangeLog = {

//tag::createPersonTable[]
    include file: 'create-person-table.groovy'
//end::createPersonTable[]

//tag::changeAgeConstraintToNullable[]    
    include file: 'change-age-constraint-to-nullable.groovy'
//end::changeAgeConstraintToNullable[]


//tag::addAddressFieldsToPerson[]
    include file: 'add-address-fields-to-person.groovy'
//end::addAddressFieldsToPerson[]

//end::createAddressTable[]
    include file: 'create-address-table.groovy'
//end::createAddressTable[]
}
