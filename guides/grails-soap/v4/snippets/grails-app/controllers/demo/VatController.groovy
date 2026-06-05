package demo

import groovy.transform.CompileStatic
import org.springframework.context.MessageSource

@CompileStatic
class VatController {

    static allowedMethods = [index: 'GET', validate: 'GET']

    VatService vatService

    CountryService countryService

    MessageSource messageSource

    def index() {
        [countries: countryService.findAll()]
    }

    def validate(VatCommand cmd) {

        if ( cmd.hasErrors() ) {
            render view: 'index', model: [cmd: cmd, countries: countryService.findAll()]
            return
        }
        boolean isValid = vatService.validateVat(cmd.code, cmd.vatNumber)
        if ( isValid ) {
            flash.message = messageSource.getMessage('vat.valid',
                    [cmd.code, cmd.vatNumber] as Object[],
                    "${cmd.code} : ${cmd.vatNumber} is valid",
                    request.locale)

        } else {
            flash.error = messageSource.getMessage('vat.valid',
                    [cmd.code, cmd.vatNumber] as Object[],
                    "${cmd.code} : ${cmd.vatNumber} is NOT valid",
                    request.locale)
        }
        render view: 'index', model: [cmd: cmd, countries: countryService.findAll()]


    }
}