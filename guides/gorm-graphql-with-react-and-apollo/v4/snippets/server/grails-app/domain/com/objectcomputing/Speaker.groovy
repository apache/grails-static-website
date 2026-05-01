package com.objectcomputing

import grails.rest.Resource
import org.grails.gorm.graphql.entity.dsl.GraphQLMapping
import java.time.LocalDate
import java.time.Period

@Resource(uri='/speaker')
class Speaker {

    String firstName
    String lastName
    String name
    String email
    String bio
    LocalDate birthday

    static hasMany = [talks: Talk]

    static graphql = GraphQLMapping.build {

        property 'lastName', order: 1 //<1>
        property 'firstName', order: 2
        property 'email', order: 3

        exclude 'birthday' //<2>

        property 'name', deprecationReason: 'To be removed August 1st, 2020' //<3>

        property('bio') { //<4>
            order 4
            dataFetcher { Speaker speaker ->
                speaker.bio ?: "No biography provided"
            }
        }

        add('age', Integer) { //<5>
            dataFetcher { Speaker speaker ->
                Period.between(speaker.birthday, LocalDate.now()).years
            }
            input false
        }
    }

    static constraints = {
        email nullable: true, email: true
        birthday nullable: true
        bio nullable: true
    }

    static mapping = {
        bio type: 'text'
        name formula: 'concat(FIRST_NAME,\' \',LAST_NAME)'
        talks sort: 'id'
    }

}