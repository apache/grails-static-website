package demo

import groovy.transform.CompileStatic

@CompileStatic
class EmailTask implements Runnable { //<1>

    String email //<2>
    String message //<3>
    EmailService emailService //<4>

    EmailTask(EmailService emailService, String email, String message) { //<5>
        this.emailService = emailService
        this.email = email
        this.message = message
    }

    @Override
    void run() { //<6>
        emailService.send(email, message)
    }
}
