package demo

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

@CompileStatic
@Slf4j
class LegalController {
    def index() {
        log.info 'inside legal controller'
        render 'Legal Terms'
    }
}
