package example.grails

import grails.testing.mixin.integration.Integration
import org.apache.grails.testing.http.client.HttpClientSupport
import spock.lang.Specification
import spock.lang.Unroll

import java.nio.file.Files
import java.nio.file.Path

@Integration
class PointOfInterestUploadSpec extends Specification implements HttpClientSupport {

    PointOfInterestDataService pointOfInterestDataService
    UploadPointOfInterestFeaturedImageService uploadPointOfInterestFeaturedImageService

    @Unroll
    void "uploading '#filename' saves '#storedName' in the folder and the app serves it"() {
        given:
        PointOfInterest poi = pointOfInterestDataService.save('Museum')
        byte[] image = UploadTestSupport.pngBytes(1024)
        Path folder = uploadPointOfInterestFeaturedImageService.cdnFolder.resolve("pointOfInterest/${poi.id}")

        when:
        def response = httpPostMultipart('/pointOfInterest/uploadFeaturedImage',
                UploadTestSupport.featuredImageBody(poi.id, poi.version, filename, 'image/png', image))

        then:
        response.assertStatus(200)
        pointOfInterestDataService.get(poi.id).featuredImageUrl == "/uploads/pointOfInterest/${poi.id}/${storedName}"
        Files.readAllBytes(folder.resolve(storedName)) == image

        and: 'nothing was written outside the folder'
        Files.list(folder).toList()*.fileName*.toString() == [storedName]

        when:
        def served = http("/uploads/pointOfInterest/${poi.id}/${storedName}")

        then:
        served.assertStatus(200)

        cleanup:
        folder?.toFile()?.deleteDir()
        pointOfInterestDataService.delete(poi.id)

        where:
        filename              || storedName
        'photo.png'           || 'photo.png'
        '../../evil.png'      || 'evil.png'
        'my photo (1).png'    || 'my_photo__1_.png'
    }
}
