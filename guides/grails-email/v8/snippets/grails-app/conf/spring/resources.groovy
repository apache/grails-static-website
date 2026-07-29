import example.AwsSesEmailService
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
            emailService(AwsSesEmailService)
            break
        case 'smtp':
            emailService(SmtpEmailService)
            break
        default:
            throw new IllegalArgumentException("Unsupported email.provider: ${provider}")
    }
    //end::emailServiceBeans[]
}
