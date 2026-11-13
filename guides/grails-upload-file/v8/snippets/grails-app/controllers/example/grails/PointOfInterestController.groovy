package example.grails

import grails.compiler.GrailsCompileStatic
import org.springframework.context.MessageSource

@GrailsCompileStatic
class PointOfInterestController {

    static allowedMethods = [
            save: 'POST',
            update: 'PUT',
            uploadFeaturedImage: 'POST',
            delete: 'DELETE',
    ]

    PointOfInterestDataService pointOfInterestDataService
    UploadPointOfInterestFeaturedImageService uploadPointOfInterestFeaturedImageService
    CrudMessageService crudMessageService
    MessageSource messageSource

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        [
                pointOfInterestList: pointOfInterestDataService.list(params),
                pointOfInterestCount: pointOfInterestDataService.count(),
        ]
    }

    def show(Long id) {
        PointOfInterest pointOfInterest = pointOfInterestDataService.get(id)
        if (!pointOfInterest) {
            notFound()
            return
        }
        [pointOfInterest: pointOfInterest]
    }

    def create() {
        [pointOfInterest: new PointOfInterest()]
    }

    def save(NameCommand cmd) {
        if (cmd.hasErrors()) {
            respond cmd.errors, model: [pointOfInterest: cmd], view: 'create'
            return
        }
        PointOfInterest pointOfInterest = pointOfInterestDataService.save(cmd.name)
        if (pointOfInterest.hasErrors()) {
            respond pointOfInterest.errors, model: [pointOfInterest: pointOfInterest], view: 'create'
            return
        }
        flash.message = crudMessage(CRUD.CREATE, pointOfInterest.id)
        redirect pointOfInterest
    }

    def edit(Long id) {
        PointOfInterest pointOfInterest = pointOfInterestDataService.get(id)
        if (!pointOfInterest) {
            notFound()
            return
        }
        [pointOfInterest: pointOfInterest]
    }

    def update(NameUpdateCommand cmd) {
        if (cmd.hasErrors()) {
            respond cmd.errors, model: [pointOfInterest: cmd], view: 'edit'
            return
        }
        PointOfInterest pointOfInterest = pointOfInterestDataService.updateName(cmd.id, cmd.version, cmd.name)
        if (pointOfInterest == null) {
            notFound()
            return
        }
        if (pointOfInterest.hasErrors()) {
            respond pointOfInterest.errors, model: [pointOfInterest: pointOfInterest], view: 'edit'
            return
        }
        flash.message = crudMessage(CRUD.UPDATE, pointOfInterest.id)
        redirect pointOfInterest
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

    // tag::uploadFeaturedImage[]
    def uploadFeaturedImage(FeaturedImageCommand cmd) {
        if (cmd.hasErrors()) {
            respond cmd.errors, model: [pointOfInterest: cmd], view: 'editFeaturedImage'
            return
        }
        PointOfInterest pointOfInterest = uploadPointOfInterestFeaturedImageService.uploadFeaturedImage(cmd) // <1>
        if (pointOfInterest == null) {
            notFound()
            return
        }
        if (pointOfInterest.hasErrors()) {
            respond pointOfInterest.errors, model: [pointOfInterest: pointOfInterest], view: 'editFeaturedImage'
            return
        }
        flash.message = crudMessage(CRUD.UPDATE, pointOfInterest.id)
        redirect pointOfInterest
    }
    // end::uploadFeaturedImage[]

    def delete(Long id) {
        if (id == null) {
            notFound()
            return
        }
        pointOfInterestDataService.delete(id)
        flash.message = crudMessage(CRUD.DELETE, id)
        redirect action: 'index', method: 'GET'
    }

    protected void notFound() {
        flash.message = crudMessage(CRUD.NOT_FOUND, params.long('id'))
        redirect action: 'index', method: 'GET'
    }

    private String crudMessage(CRUD crud, Long id) {
        Locale locale = request.locale
        String domainName = messageSource.getMessage('pointOfInterest.label', [] as Object[], 'Point of Interest', locale)
        crudMessageService.message(crud, domainName, id, locale)
    }
}
