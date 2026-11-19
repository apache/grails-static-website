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

    Hotel uploadFeaturedImage(FeaturedImageCommand cmd) {
        Hotel hotel = hotelDataService.get(cmd.id) // <3>
        if (!hotel) {
            return null
        }
        if (StaleVersion.reject(hotel.errors, hotel.version, cmd.version, 'Hotel')) { // <4>
            return hotel
        }
        String previousKey = hotel.featuredImageKey

        String key = "hotel/${cmd.id}/${UUID.randomUUID()}-${cmd.sanitizedFilename()}" // <5>
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(cmd.featuredImageFile.contentType)
                .build()
        cmd.featuredImageFile.inputStream.withCloseable { InputStream input ->
            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(input, cmd.featuredImageFile.size)) // <6>
        }

        String featuredImageUrl = s3Client.utilities()
                .getUrl(GetUrlRequest.builder().bucket(bucket).key(key).build())
                .toExternalForm() // <7>

        try {
            hotel = hotelDataService.updateFeaturedImage(cmd.id, cmd.version, key, featuredImageUrl) // <8>
        } catch (Exception e) {
            deleteFileByKey(key) // <9>
            throw e
        }
        if (!hotel || hotel.hasErrors()) {
            deleteFileByKey(key) // <9>
        } else if (previousKey) {
            deleteFileByKey(previousKey) // <10>
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
