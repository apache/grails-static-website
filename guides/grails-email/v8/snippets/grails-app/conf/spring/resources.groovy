import example.AwsSesMailService
import example.SendGridEmailService
import example.SmtpEmailService
import grails.util.Environment

beans = {
    if (Environment.current == Environment.TEST) {
        return
    }
    //tag::emailServiceBeans[]
    def provider = application.config.getProperty('email.provider', String, 'smtp')
    switch (provider) {
        case 'sendgrid':
            emailService(SendGridEmailService)
            break
        case 'ses':
            emailService(AwsSesMailService)
            break
        default:
            emailService(SmtpEmailService)
    }
    //end::emailServiceBeans[]
}
