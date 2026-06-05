package demo

import groovy.transform.CompileStatic

@CompileStatic
class RandomPersonService {

    String randomOciPersonName() {
        List<String> people = OCI.PEOPLE
        Collections.shuffle(people)
        people.first()
    }
}

