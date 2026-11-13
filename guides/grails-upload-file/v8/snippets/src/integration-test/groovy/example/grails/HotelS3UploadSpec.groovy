package example.grails

import grails.testing.mixin.integration.Integration
import org.apache.grails.testing.http.client.HttpClientSupport
import org.testcontainers.DockerClientFactory
import org.testcontainers.localstack.LocalStackContainer
import org.testcontainers.utility.DockerImageName
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.CreateBucketRequest
import software.amazon.awssdk.services.s3.model.HeadObjectRequest
import software.amazon.awssdk.services.s3.model.HeadObjectResponse
import spock.lang.Requires
import spock.lang.Shared
import spock.lang.Specification

@Integration
@Requires({ DockerClientFactory.instance().isDockerAvailable() }) // <1>
class HotelS3UploadSpec extends Specification implements HttpClientSupport {

    static final String BUCKET = 'grails-guides-test'

    @Shared
    LocalStackContainer localstack = new LocalStackContainer(DockerImageName.parse('localstack/localstack:4.4.0'))
            .withServices('s3') // <2>

    @Shared
    S3Client localstackS3Client

    HotelDataService hotelDataService
    UploadHotelFeaturedImageService uploadHotelFeaturedImageService

    S3Client originalS3Client
    String originalBucket

    void setupSpec() {
        localstack.start()
        localstackS3Client = S3Client.builder()
                .endpointOverride(localstack.endpoint)
                .region(Region.of(localstack.region))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(localstack.accessKey, localstack.secretKey)))
                .forcePathStyle(true)
                .build()
        localstackS3Client.createBucket(CreateBucketRequest.builder().bucket(BUCKET).build())
    }

    void cleanupSpec() {
        localstackS3Client?.close()
        localstack.stop()
    }

    void setup() { // <3>
        originalS3Client = uploadHotelFeaturedImageService.s3Client
        originalBucket = uploadHotelFeaturedImageService.bucket
        uploadHotelFeaturedImageService.s3Client = localstackS3Client
        uploadHotelFeaturedImageService.bucket = BUCKET
    }

    void cleanup() {
        uploadHotelFeaturedImageService.s3Client = originalS3Client
        uploadHotelFeaturedImageService.bucket = originalBucket
    }

    void "a featured image is uploaded to S3 and the hotel keeps its key and URL"() {
        given:
        Hotel hotel = hotelDataService.save('Grand')
        byte[] image = UploadTestSupport.pngBytes(2 * 1024 * 1024)

        when:
        def response = httpPostMultipart('/hotel/uploadFeaturedImage',
                UploadTestSupport.featuredImageBody(hotel.id, hotel.version, '../my photo.png', 'image/png', image))

        then:
        response.assertStatus(200)

        when:
        Hotel updated = hotelDataService.get(hotel.id)
        HeadObjectResponse object = localstackS3Client.headObject(
                HeadObjectRequest.builder().bucket(BUCKET).key(updated.featuredImageKey).build())

        then:
        updated.featuredImageKey == "hotel/${hotel.id}/my_photo.png"
        updated.featuredImageUrl.endsWith("/${BUCKET}/hotel/${hotel.id}/my_photo.png")
        object.contentLength() == image.length
        object.contentType() == 'image/png'

        cleanup:
        hotelDataService.delete(hotel.id)
    }

    void "an S3 error is shown on the upload form and the hotel is not changed"() {
        given:
        Hotel hotel = hotelDataService.save('Grand')
        uploadHotelFeaturedImageService.bucket = 'grails-guides-missing'

        when:
        def response = httpPostMultipart('/hotel/uploadFeaturedImage',
                UploadTestSupport.featuredImageBody(hotel.id, hotel.version, 'photo.png', 'image/png',
                        UploadTestSupport.pngBytes(1024)))

        then:
        response.assertStatus(200)
        response.assertContains('alert-danger')
        response.assertContains('The featured image could not be uploaded to S3')
        hotelDataService.get(hotel.id).featuredImageKey == null

        cleanup:
        hotelDataService.delete(hotel.id)
    }
}
