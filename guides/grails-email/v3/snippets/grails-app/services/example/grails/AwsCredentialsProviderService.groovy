package example.grails

import com.amazonaws.auth.AWSCredentials
import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import grails.config.Config
import grails.core.support.GrailsConfigurationAware
import groovy.transform.CompileStatic

@CompileStatic
class AwsCredentialsProviderService implements AWSCredentialsProvider, GrailsConfigurationAware { // <1>

    String accessKey

    String secretKey

    @Override
    AWSCredentials getCredentials() {
        return new BasicAWSCredentials(accessKey, secretKey)
    }

    @Override
    void refresh() {

    }

    @Override
    void setConfiguration(Config co) {
        this.accessKey = co.getProperty('aws.accessKeyId', String)
        if (!this.accessKey) {
            throw new IllegalStateException('aws.accessKeyId not set')
        }
        this.secretKey = co.getProperty('aws.secretKey', String)
        if (!this.secretKey) {
            throw new IllegalStateException('aws.secretKey not set')
        }
    }
}