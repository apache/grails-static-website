package example.grails

import grails.config.Config
import grails.core.support.GrailsConfigurationAware
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

@Slf4j
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
        PointOfInterest poi = pointOfInterestDataService.get(cmd.id) // <2>
        if (!poi) {
            return null
        }
        String previousUrl = poi.featuredImageUrl

        String filename = "${UUID.randomUUID()}-${cmd.sanitizedFilename()}" // <3>
        Path folder = cdnFolder.resolve("pointOfInterest/${cmd.id}")
        Files.createDirectories(folder)
        Path file = folder.resolve(filename)
        cmd.featuredImageFile.transferTo(file) // <4>

        String featuredImageUrl = "${cdnRootUrl}/pointOfInterest/${cmd.id}/${filename}"
        try {
            poi = pointOfInterestDataService.updateFeaturedImageUrl(cmd.id, cmd.version, featuredImageUrl) // <5>
        } catch (Exception e) {
            deleteFile(file) // <6>
            throw e
        }
        if (!poi || poi.hasErrors()) {
            deleteFile(file) // <6>
        } else {
            deleteFileByUrl(previousUrl) // <7>
        }
        poi
    }

    boolean deleteFileByUrl(String featuredImageUrl) {
        String prefix = "${cdnRootUrl}/"
        if (!featuredImageUrl?.startsWith(prefix)) {
            return false
        }
        Path file = cdnFolder.resolve(featuredImageUrl.substring(prefix.length())).normalize()
        if (!file.startsWith(cdnFolder)) { // <8>
            return false
        }
        deleteFile(file)
    }

    private boolean deleteFile(Path file) {
        try {
            return Files.deleteIfExists(file)
        } catch (IOException e) {
            log.warn('could not remove file {}', file, e)
            return false
        }
    }
}
