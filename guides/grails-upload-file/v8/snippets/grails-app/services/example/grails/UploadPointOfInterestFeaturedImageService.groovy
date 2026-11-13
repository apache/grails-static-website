package example.grails

import grails.config.Config
import grails.core.support.GrailsConfigurationAware
import groovy.transform.CompileStatic

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

@CompileStatic
class UploadPointOfInterestFeaturedImageService implements GrailsConfigurationAware {

    PointOfInterestDataService pointOfInterestDataService

    Path cdnFolder
    String cdnRootUrl

    @Override
    void setConfiguration(Config co) { // <1>
        cdnFolder = Paths.get(co.getRequiredProperty('grails.guides.cdnFolder')).toAbsolutePath().normalize()
        cdnRootUrl = co.getRequiredProperty('grails.guides.cdnRootUrl')
    }

    PointOfInterest uploadFeaturedImage(FeaturedImageCommand cmd) {
        String filename = cmd.sanitizedFilename()
        Path folder = cdnFolder.resolve("pointOfInterest/${cmd.id}")
        Files.createDirectories(folder)
        Path file = folder.resolve(filename)
        cmd.featuredImageFile.transferTo(file) // <2>

        String featuredImageUrl = "${cdnRootUrl}/pointOfInterest/${cmd.id}/${filename}"
        PointOfInterest poi = pointOfInterestDataService.updateFeaturedImageUrl(cmd.id, cmd.version, featuredImageUrl) // <3>
        if (!poi || poi.hasErrors()) {
            Files.deleteIfExists(file) // <4>
        }
        poi
    }
}
