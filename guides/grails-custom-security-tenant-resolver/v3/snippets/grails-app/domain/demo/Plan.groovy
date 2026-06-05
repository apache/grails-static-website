package demo

import grails.gorm.MultiTenant

class Plan implements MultiTenant<Plan> { // <1>
    String title
    String username

    static mapping = {
        tenantId name: 'username' // <2>
    }
}
