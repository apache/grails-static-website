package example.grails

import grails.gorm.transactions.Transactional
import grails.plugin.awssdk.s3.AmazonS3Service
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

@CompileStatic
@Slf4j
@Transactional
class UploadHotelFeaturedImageService {

    AmazonS3Service amazonS3Service

    HotelDataService hotelDataService

    Hotel uploadFeatureImage(FeaturedImageCommand cmd) {
        String path = "hotel/${cmd.id}/${cmd.featuredImageFile.originalFilename}"
        String s3FileUrl = amazonS3Service.storeMultipartFile(path, cmd.featuredImageFile)

        Hotel hotel = hotelDataService.update(cmd.id, cmd.version, path, s3FileUrl)
        if ( !hotel || hotel.hasErrors() ) {
            deleteFileByPath(path)
        }
        hotel
    }

    boolean deleteFileByPath(String path) {
        boolean result = amazonS3Service.deleteFile(path)
        if (!result) {
            log.warn 'could not remove file {}', path
        }
        result
    }
}
