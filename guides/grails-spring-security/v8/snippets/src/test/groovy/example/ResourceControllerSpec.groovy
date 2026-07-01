package example

import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import spock.lang.Specification

class ResourceControllerSpec extends Specification implements ControllerUnitTest<ResourceController>, DataTest {

    Class<?>[] getDomainClassesToMock() {
        [Resource, User, Role, UserRole] as Class[]
    }

    void 'index lists resources without authentication'() {
        given:
        new Resource(name: 'Public').save(flush: true)

        when:
        controller.index()

        then:
        model.resourceList
        model.resourceCount == 1
    }

    void 'show returns 404 when resource missing'() {
        when:
        controller.show(99L)

        then:
        response.status == 404
    }

    void 'save creates resource when valid'() {
        given:
        request.method = 'POST'
        request.JSON = [name: 'Team Notes', description: 'Shared notes']

        when:
        controller.save()

        then:
        response.status == 201
        Resource.count() == 1
        Resource.first().name == 'Team Notes'
    }

    void 'update persists changes when valid'() {
        given:
        def resource = new Resource(name: 'Original', description: 'Before').save(flush: true)
        request.method = 'PUT'
        request.JSON = [name: 'Updated', description: 'After']

        when:
        controller.update(resource.id)

        then:
        response.status == 200
        resource.refresh().name == 'Updated'
    }

    void 'update returns 404 when resource missing'() {
        given:
        request.method = 'PUT'
        request.JSON = [name: 'Nobody']

        when:
        controller.update(99L)

        then:
        response.status == 404
    }

    void 'delete removes resource'() {
        given:
        def resource = new Resource(name: 'Disposable').save(flush: true)
        request.method = 'DELETE'

        when:
        controller.delete(resource.id)

        then:
        response.status == 204
        Resource.count() == 0
    }

    void 'delete returns 404 when resource missing'() {
        given:
        request.method = 'DELETE'

        when:
        controller.delete(99L)

        then:
        response.status == 404
    }
}
