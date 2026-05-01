import example.grails.AwsSesMailService
import example.grails.SendGridEmailService

beans = {
    if ( System.getProperty('AWS_REGION') && System.getProperty('AWS_SOURCE_EMAIL') && System.getProperty('AWS_ACCESS_KEY_ID') && System.getProperty('AWS_SECRET_KEY') ) {
        emailService(AwsSesMailService) {
            awsCredentialsProviderService = ref('awsCredentialsProviderService')
        }
    } else if ( System.getProperty('SENDGRID_FROM_EMAIL') && System.getProperty('SENDGRID_APIKEY') ) {
        emailService(SendGridEmailService)
    }
}
