package example.grails

import groovy.transform.CompileStatic
import org.springframework.validation.Errors

@CompileStatic
class StaleVersion {

    static boolean reject(Errors errors, Long currentVersion, Long formVersion, String entityName) {
        if (formVersion == null || currentVersion == null || currentVersion <= formVersion) { // <1>
            return false
        }
        errors.rejectValue('version', 'default.optimistic.locking.failure', [entityName] as Object[], // <2>
                "Another user has updated this ${entityName} while you were editing".toString())
        true
    }
}
