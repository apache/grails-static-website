package demo

import groovy.util.logging.Slf4j
import groovy.transform.CompileStatic

@Slf4j
@CompileStatic
class UploadBookFeaturedImageService {

    BookGormService bookGormService

    GoogleCloudStorageService googleCloudStorageService

    private static String fileSuffix() {
        new Date().format('-YYYY-MM-dd-HHmmssSSS')
    }

    Book uploadFeaturedImage(FeaturedImageCommand cmd) {
        String fileName = "${cmd.featuredImageFile.originalFilename}${fileSuffix()}"

        log.info "cloud storage file name $fileName"

        String fileUrl = googleCloudStorageService.storeMultipartFile(fileName, cmd.featuredImageFile)

        log.info "cloud storage media url $fileUrl"

        def book = bookGormService.updateFeaturedImageUrl(cmd.id, cmd.version, fileName, fileUrl)
        if ( !book || book.hasErrors() ) {
            googleCloudStorageService.deleteFile(fileName)
        }
        book
    }
}
