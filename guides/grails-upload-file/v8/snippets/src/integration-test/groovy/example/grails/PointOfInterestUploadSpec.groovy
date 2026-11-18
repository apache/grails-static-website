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
    void "uploading '#filename' saves a file ending in '#storedName' in the folder and the app serves it"() {
        given:
        PointOfInterest poi = pointOfInterestDataService.save('Museum')
        byte[] image = UploadTestSupport.pngBytes(1024)
        Path folder = folderOf(poi.id)

        when:
        def response = httpPostMultipart('/pointOfInterest/uploadFeaturedImage',
                UploadTestSupport.featuredImageBody(poi.id, poi.version, filename, 'image/png', image))
        List<String> files = filesIn(folder)

        then:
        response.assertStatus(200)
        files.size() == 1
        UploadTestSupport.isUniqueName(files[0], storedName)
        pointOfInterestDataService.get(poi.id).featuredImageUrl == "/uploads/pointOfInterest/${poi.id}/${files[0]}"
        Files.readAllBytes(folder.resolve(files[0])) == image

        when:
        def served = http("/uploads/pointOfInterest/${poi.id}/${files[0]}")

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
        'x' * 300 + '.png'    || 'x' * 60 + '.png'
    }

    void "a new image replaces the previous file, and deleting the point of interest removes it"() {
        given:
        PointOfInterest poi = pointOfInterestDataService.save('Museum')
        Path folder = folderOf(poi.id)
        httpPostMultipart('/pointOfInterest/uploadFeaturedImage',
                UploadTestSupport.featuredImageBody(poi.id, poi.version, 'photo.png', 'image/png',
                        UploadTestSupport.pngBytes(1024)))
        PointOfInterest first = pointOfInterestDataService.get(poi.id)

        when: 'a file with the same name is uploaded again'
        httpPostMultipart('/pointOfInterest/uploadFeaturedImage',
                UploadTestSupport.featuredImageBody(poi.id, first.version, 'photo.png', 'image/png',
                        UploadTestSupport.pngBytes(2048)))
        PointOfInterest second = pointOfInterestDataService.get(poi.id)

        then: 'the new file gets its own URL, and the previous file is deleted'
        second.featuredImageUrl != first.featuredImageUrl
        filesIn(folder) == [second.featuredImageUrl.tokenize('/').last()]

        when:
        httpDelete("/pointOfInterest/delete/${poi.id}").assertStatus(200)

        then:
        pointOfInterestDataService.get(poi.id) == null
        filesIn(folder).empty

        cleanup:
        folder?.toFile()?.deleteDir()
    }

    void "an upload from an outdated form is rejected and its file is deleted"() {
        given:
        PointOfInterest poi = pointOfInterestDataService.save('Museum')
        Path folder = folderOf(poi.id)
        Long version = poi.version
        httpPostMultipart('/pointOfInterest/uploadFeaturedImage',
                UploadTestSupport.featuredImageBody(poi.id, version, 'first.png', 'image/png',
                        UploadTestSupport.pngBytes(1024)))
        String currentUrl = pointOfInterestDataService.get(poi.id).featuredImageUrl

        when: 'a second form, opened before the first upload, is submitted'
        def response = httpPostMultipart('/pointOfInterest/uploadFeaturedImage',
                UploadTestSupport.featuredImageBody(poi.id, version, 'second.png', 'image/png',
                        UploadTestSupport.pngBytes(1024)))

        then:
        response.assertStatus(200)
        response.assertContains('Another user has updated this Point of Interest while you were editing')
        pointOfInterestDataService.get(poi.id).featuredImageUrl == currentUrl
        filesIn(folder) == [currentUrl.tokenize('/').last()]

        cleanup:
        folder?.toFile()?.deleteDir()
        pointOfInterestDataService.delete(poi.id)
    }

    private Path folderOf(Long id) {
        uploadPointOfInterestFeaturedImageService.cdnFolder.resolve("pointOfInterest/${id}")
    }

    private static List<String> filesIn(Path folder) {
        Files.list(folder).withCloseable { it.toList()*.fileName*.toString() }
    }
}
