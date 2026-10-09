package com.grails.example

import org.springframework.security.access.prepost.PreAuthorize;

@PreAuthorize("hasRole('ADMIN')")
class AdminController {

    def index() {
        render(view: 'index')
    }

}
