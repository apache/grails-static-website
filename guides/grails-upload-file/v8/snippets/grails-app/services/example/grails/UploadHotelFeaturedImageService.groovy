package example.grails

import grails.config.Config
import grails.core.support.GrailsConfigurationAware
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import software.amazon.awssdk.core.exception.SdkException
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest
import software.amazon.awssdk.services.s3.model.GetUrlRequest
import software.amazon.awssdk.services.s3.model.PutObjectRequest

@Slf4j
@CompileStatic
class UploadHotelFeaturedImageService implements GrailsConfigurationAware {

    S3Client s3Client // <1>
    HotelDataService hotelDataService

    String bucket

    @Override
    void setConfiguration(Config co) {
        bucket = co.getRequiredProperty('aws.s3.bucket') // <2>
    }

    Hotel uploadFeatureImage(FeaturedImageCommand cmd) {
        String key = "hotel/${cmd.id}/${cmd.sanitizedFilename()}"
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(cmd.featuredImageFile.contentType)
                .build()
        s3Client.putObject(putObjectRequest,
                RequestBody.fromInputStream(cmd.featuredImageFile.inputStream, cmd.featuredImageFile.size)) // <3>

        String featuredImageUrl = s3Client.utilities()
                .getUrl(GetUrlRequest.builder().bucket(bucket).key(key).build())
                .toExternalForm() // <4>

        Hotel hotel = hotelDataService.update(cmd.id, cmd.version, key, featuredImageUrl) // <5>
        if (!hotel || hotel.hasErrors()) {
            deleteFileByKey(key) // <6>
        }
        hotel
    }

    boolean deleteFileByKey(String key) {
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(key).build())
            return true
        } catch (SdkException e) {
            log.warn('could not remove file {}', key, e)
            return false
        }
    }
}
