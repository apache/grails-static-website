package com.grails.example

import org.springframework.security.access.prepost.PreAuthorize

@PreAuthorize("hasRole('USER')")
class AccountController {
    def index() {
        render(view: 'index')
    }
}
