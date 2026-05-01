package demo

import grails.gorm.MultiTenant

class Plan implements MultiTenant<Plan> { // <1>
    String title
}
