import example.grails.S3ClientFactory
import example.grails.UploadsWebMvcConfigurer

beans = {
    // tag::uploadsWebMvcConfigurer[]
    uploadsWebMvcConfigurer(UploadsWebMvcConfigurer, application.config.getRequiredProperty('grails.guides.cdnFolder'))
    // end::uploadsWebMvcConfigurer[]

    // tag::s3Client[]
    s3Client(S3ClientFactory,
            application.config.getProperty('aws.s3.region', 'us-east-1'),
            application.config.getProperty('aws.s3.endpoint', '')) { bean ->
        bean.factoryMethod = 'create'
        bean.destroyMethod = 'close'
    }
    // end::s3Client[]
}
