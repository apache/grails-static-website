package demo

import geb.Page

class LoginPage extends Page {

    static url = 'login/auth'

    static at = { title == 'Login' }

    static content = {
        usernameField { $('#username', 0) }
        passwordField { $('#password', 0) }
        positionField { $('#coordinatePosition', 0)}
        valueField { $('#coordinateValue', 0) }
        submitField { $('#submit', 0) }
    }

    String position() {
        positionField.getAttribute('value')
    }

    void login(String username, String password, String value) {
        usernameField << username
        passwordField << password
        valueField << value
        submitField.click()
    }
}
