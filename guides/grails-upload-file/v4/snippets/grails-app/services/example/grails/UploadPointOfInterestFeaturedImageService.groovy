package example.grails

import grails.config.Config
import grails.core.support.GrailsConfigurationAware
import groovy.transform.CompileStatic

@SuppressWarnings('GrailsStatelessService')
@CompileStatic
class UploadPointOfInterestFeaturedImageService implements GrailsConfigurationAware {

    PointOfInterestDataService pointOfInterestDataService

    String cdnFolder
    String cdnRootUrl

    @Override
    void setConfiguration(Config co) {
        cdnFolder = co.getRequiredProperty('grails.guides.cdnFolder')
        cdnRootUrl = co.getRequiredProperty('grails.guides.cdnRootUrl')
    }

    @SuppressWarnings('JavaIoPackageAccess')
    PointOfInterest uploadFeatureImage(FeaturedImageCommand cmd) {

        String filename = cmd.featuredImageFile.originalFilename
        String folderPath = "${cdnFolder}/pointOfInterest/${cmd.id}"
        File folder = new File(folderPath)
        if ( !folder.exists() ) {
            folder.mkdirs()
        }
        String path = "${folderPath}/${filename}"
        cmd.featuredImageFile.transferTo(new File(path))

        String featuredImageUrl = "${cdnRootUrl}//pointOfInterest/${cmd.id}/${filename}"
        PointOfInterest poi = pointOfInterestDataService.updateFeaturedImageUrl(cmd.id, cmd.version, featuredImageUrl)

        if ( !poi || poi.hasErrors() ) {
            File f = new File(path)
            f.delete()
        }
        poi
    }
}
