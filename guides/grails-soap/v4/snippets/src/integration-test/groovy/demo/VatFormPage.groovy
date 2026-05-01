package demo

import geb.Page

class VatFormPage extends Page {

    static url = '/vat/index'

    static content = {
        vatNumberInput { $('input#vatNumber', 0) }
        codeSelect { $('select#code', 0) }
        submitButton { $('input#submit', 0) }
    }

    void validate(String code, String vatNumber ) {
        vatNumberInput = vatNumber
        codeSelect = code.toUpperCase()
        submitButton.click()
    }
}
