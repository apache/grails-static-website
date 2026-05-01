package demo

import geb.Page
import geb.module.TextInput

class SignUpPage extends Page {

    static url = '/signup'

    static content = {
        firstNameInput { $(name: "firstName").module(TextInput) }
        lastNameInput { $(name: "lastName").module(TextInput) }
        emailInput { $(name: "email").module(TextInput) }
        submitButton {  $('#submit') }
    }

    void submit(String firstName, String lastName, String email) {
        firstNameInput.text = firstName
        lastNameInput.text = lastName
        emailInput.text = email
        submitButton.click()
    }
}
