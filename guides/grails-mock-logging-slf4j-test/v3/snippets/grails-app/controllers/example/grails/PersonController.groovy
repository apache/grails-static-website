// tag::personControllerCreate[]
package example.grails

import grails.validation.ValidationException
import groovy.transform.CompileStatic

@CompileStatic
class PersonController {

    static scaffold = Person

    PersonDataService personDataService

    def createPerson(String name, Integer age) {
        try {
            Person person = personDataService.savePerson(name, age)
            log.info "person saved successfully: ${person}"
            respond person, view: 'show'
        } catch (ValidationException e) {
            log.error "Error occurred on save!"
            redirect action: "index"
        }
    }
// end::personControllerCreate[]

    // tag::personControllerOfferAdvice[]
    def offerAdvice(String name) {
        AgeAdvisor ageAdvisor = new AgeAdvisor()

        Person person = personDataService.findPerson(name)
        if (person) {
            ageAdvisor.offerAgeAdvice(person.age)
        } else {
            log.error "No person by name ${name} found."
        }
        redirect action: "index"
    }
    // end::personControllerOfferAdvice[]

// tag::personControllerEnd[]
}
// end::personControllerEnd[]