package example.grails

import org.springframework.context.MessageSource
import groovy.transform.CompileStatic

@CompileStatic
@SuppressWarnings('LineLength')
class PointOfInterestController {

    static allowedMethods = [
            save: 'POST',
            update: 'PUT',
            uploadFeaturedImage: 'POST',
            delete: 'DELETE',
    ]

    UploadPointOfInterestFeaturedImageService uploadPointOfInterestFeaturedImageService

    PointOfInterestDataService pointOfInterestDataService

    MessageSource messageSource

    CrudMessageService crudMessageService

    private String domainName(Locale locale) {
        messageSource.getMessage('pointOfInterest.label',
                [] as Object[],
                'Point of Interest',
                locale)
    }

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        [
                pointOfInterestList: pointOfInterestDataService.list(params),
                pointOfInterestCount: pointOfInterestDataService.count(),
        ]
    }

    // tag::editFeaturedImage[]
    def editFeaturedImage(Long id) {
        PointOfInterest pointOfInterest = pointOfInterestDataService.get(id)
        if (!pointOfInterest) {
            notFound()
            return
        }
        [pointOfInterest: pointOfInterest]
    }
    // end::editFeaturedImage[]

    def show(Long id) {
        PointOfInterest pointOfInterest = pointOfInterestDataService.get(id)
        if (!pointOfInterest) {
            notFound()
            return
        }
        [pointOfInterest: pointOfInterest]
    }

    @SuppressWarnings(['FactoryMethodName'])
    def create() {
        [pointOfInterest: new PointOfInterest()]
    }

    def edit(Long id) {
        PointOfInterest pointOfInterest = pointOfInterestDataService.get(id)
        if (!pointOfInterest) {
            notFound()
            return
        }
        [pointOfInterest: pointOfInterest]
    }

    // tag::uploadFeaturedImage[]
    def uploadFeaturedImage(FeaturedImageCommand cmd) {

        if (cmd.hasErrors()) {
            respond(cmd, model: [pointOfInterest: cmd], view: 'editFeaturedImage')
            return
        }

        PointOfInterest pointOfInterest = uploadPointOfInterestFeaturedImageService.uploadFeatureImage(cmd)

        if (pointOfInterest == null) {
            notFound()
            return
        }

        if (pointOfInterest.hasErrors()) {
            respond(pointOfInterest, model: [pointOfInterest: pointOfInterest], view: 'editFeaturedImage')
            return
        }

        Locale locale = request.locale
        flash.message = crudMessageService.message(CRUD.UPDATE, domainName(locale), pointOfInterest.id, locale)
        redirect pointOfInterest
    }
    // end::uploadFeaturedImage[]

    def save(NameCommand cmd) {
        if (cmd == null) {
            notFound()
            return
        }

        if (cmd.hasErrors()) {
            respond cmd.errors, model: [pointOfInterest: cmd], view: 'create'
            return
        }

        PointOfInterest pointOfInterest = pointOfInterestDataService.save(cmd.name)

        if (pointOfInterest == null) {
            notFound()
            return
        }

        if (pointOfInterest.hasErrors()) {
            respond pointOfInterest.errors, model: [pointOfInterest: pointOfInterest], view: 'create'
            return
        }

        Locale locale = request.locale
        flash.message = crudMessageService.message(CRUD.CREATE, domainName(locale), pointOfInterest.id, locale)
        redirect pointOfInterest
    }

    def update(NameUpdateCommand cmd) {
        if (cmd == null) {
            notFound()
            return
        }

        if (cmd.hasErrors()) {
            respond cmd.errors, model: [pointOfInterest: cmd], view:'edit'
            return
        }

        PointOfInterest pointOfInterest = pointOfInterestDataService.updateName(cmd.id, cmd.version, cmd.name)
        if ( !pointOfInterest ) {
            notFound()
            return
        }
        if (pointOfInterest.hasErrors()) {
            respond pointOfInterest, model: [pointOfInterest: pointOfInterest], view:'edit'
            return
        }

        Locale locale = request.locale
        flash.message = crudMessageService.message(CRUD.UPDATE, domainName(locale), pointOfInterest.id, locale)
        redirect pointOfInterest
    }

    def delete(Long id) {
        if (id == null) {
            notFound()
            return
        }

        pointOfInterestDataService.delete(id)

        Locale locale = request.locale
        flash.message = crudMessageService.message(CRUD.DELETE, domainName(locale), id, locale)
        redirect action: 'index', method: 'GET'
    }

    protected void notFound() {
        Locale locale = request.locale
        flash.message = crudMessageService.message(CRUD.NOT_FOUND, domainName(locale), params.long('id'), locale)
        redirect action: 'index', method: 'GET'
    }
}
