package example.grails

import grails.testing.mixin.integration.Integration
import org.apache.grails.testing.http.client.HttpClientSupport
import spock.lang.Specification

@Integration
class RestaurantUploadSpec extends Specification implements HttpClientSupport {

    RestaurantDataService restaurantDataService

    void "a 2MB PNG is stored in the database and served back"() {
        given: 'an image above Spring Boot default 1MB limit'
        Restaurant restaurant = restaurantDataService.save('Diner')
        byte[] image = UploadTestSupport.pngBytes(2 * 1024 * 1024)

        when:
        def response = httpPostMultipart('/restaurant/uploadFeaturedImage',
                UploadTestSupport.featuredImageBody(restaurant.id, restaurant.version, 'photo.png', 'image/png', image))

        then: 'the redirect to the show page is followed'
        response.assertStatus(200)
        response.assertContains("/restaurant/featuredImage/${restaurant.id}")

        when:
        def imageResponse = http("/restaurant/featuredImage/${restaurant.id}")

        then:
        imageResponse.assertStatus(200)
        imageResponse.headerValue('Content-Type').startsWith('image/png')
        restaurantDataService.get(restaurant.id).featuredImageBytes.length == image.length

        cleanup:
        restaurantDataService.delete(restaurant.id)
    }

    void "a text file is rejected and nothing is stored"() {
        given:
        Restaurant restaurant = restaurantDataService.save('Diner')

        when:
        def response = httpPostMultipart('/restaurant/uploadFeaturedImage',
                UploadTestSupport.featuredImageBody(restaurant.id, restaurant.version, 'notes.txt', 'text/plain', 'hello'.bytes))

        then:
        response.assertStatus(200)
        response.assertContains('alert-danger')
        response.assertContains('Choose a JPG or PNG image to upload')
        restaurantDataService.get(restaurant.id).featuredImageBytes == null

        cleanup:
        restaurantDataService.delete(restaurant.id)
    }

    void "an upload from an outdated form is rejected and the newer image is kept"() {
        given:
        Restaurant restaurant = restaurantDataService.save('Diner')
        Long version = restaurant.version
        httpPostMultipart('/restaurant/uploadFeaturedImage',
                UploadTestSupport.featuredImageBody(restaurant.id, version, 'first.png', 'image/png',
                        UploadTestSupport.pngBytes(1024)))

        when: 'a second form, opened before the first upload, is submitted'
        def response = httpPostMultipart('/restaurant/uploadFeaturedImage',
                UploadTestSupport.featuredImageBody(restaurant.id, version, 'second.png', 'image/png',
                        UploadTestSupport.pngBytes(2048)))

        then:
        response.assertStatus(200)
        response.assertContains('Another user has updated this Restaurant while you were editing')
        restaurantDataService.get(restaurant.id).featuredImageBytes.length == 1024

        cleanup:
        restaurantDataService.delete(restaurant.id)
    }
}
