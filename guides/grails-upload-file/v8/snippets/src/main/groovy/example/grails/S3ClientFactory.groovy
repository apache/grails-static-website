package example.grails

import groovy.transform.CompileStatic
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.S3ClientBuilder

@CompileStatic
class S3ClientFactory {

    static S3Client create(String region, String endpoint) {
        S3ClientBuilder builder = S3Client.builder()
                .region(Region.of(region)) // <1>
        if (endpoint) {
            builder.endpointOverride(URI.create(endpoint)) // <2>
                    .forcePathStyle(true)
        }
        builder.build() // <3>
    }
}
