package demo

import groovy.transform.CompileStatic

@CompileStatic
class NameService {

    PersonGormService personGormService

    List<Name> findAllPersonNames() {
        personGormService.findAllByActive().collect { Person person ->
          nameWithFullName(person.name)
        }
    }

    Name nameWithFullName(String fullname) {
        if ( fullname.contains('del ') ) {
            return nameWithSeparator(fullname, 'del')
        }
        return nameWithSeparator(fullname, ' ')

    }

    Name nameWithSeparator(String fullname, String separator) {
        String firstName
        String lastName
        String[] arr = fullname.split(separator)
        if ( arr.length > 0 ) {
            firstName = "${arr[0]}${separator}".toString()
            int from = firstName.length()
            lastName = fullname.substring(from, fullname.length())
        } else {
            firstName = fullname
        }
        new Name(firstName: firstName?.trim(), lastName: lastName?.trim())
    }
}
