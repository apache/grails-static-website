package example.grails

import grails.compiler.GrailsCompileStatic
import org.springframework.context.MessageSource
import org.springframework.dao.OptimisticLockingFailureException
import software.amazon.awssdk.core.exception.SdkException

@GrailsCompileStatic
class HotelController {

    static allowedMethods = [
            save: 'POST',
            update: 'PUT',
            uploadFeaturedImage: 'POST',
            delete: 'DELETE',
    ]

    HotelDataService hotelDataService
    UploadHotelFeaturedImageService uploadHotelFeaturedImageService
    CrudMessageService crudMessageService
    MessageSource messageSource

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        [
                hotelList: hotelDataService.list(params),
                hotelCount: hotelDataService.count(),
        ]
    }

    def show(Long id) {
        Hotel hotel = hotelDataService.get(id)
        if (!hotel) {
            notFound()
            return
        }
        [hotel: hotel]
    }

    def create() {
        [hotel: new Hotel()]
    }

    def save(NameCommand cmd) {
        if (cmd.hasErrors()) {
            respond cmd.errors, model: [hotel: cmd], view: 'create'
            return
        }
        Hotel hotel = hotelDataService.save(cmd.name)
        if (hotel.hasErrors()) {
            respond hotel.errors, model: [hotel: hotel], view: 'create'
            return
        }
        flash.message = crudMessage(CRUD.CREATE, hotel.id)
        redirect hotel
    }

    def edit(Long id) {
        Hotel hotel = hotelDataService.get(id)
        if (!hotel) {
            notFound()
            return
        }
        [hotel: hotel]
    }

    def update(NameUpdateCommand cmd) {
        if (cmd.hasErrors()) {
            respond cmd.errors, model: [hotel: cmd], view: 'edit'
            return
        }
        Hotel hotel = hotelDataService.updateName(cmd.id, cmd.version, cmd.name)
        if (hotel == null) {
            notFound()
            return
        }
        if (hotel.hasErrors()) {
            respond hotel.errors, model: [hotel: hotel], view: 'edit'
            return
        }
        flash.message = crudMessage(CRUD.UPDATE, hotel.id)
        redirect hotel
    }

    // tag::editFeaturedImage[]
    def editFeaturedImage(Long id) {
        Hotel hotel = hotelDataService.get(id)
        if (!hotel) {
            notFound()
            return
        }
        [hotel: hotel]
    }
    // end::editFeaturedImage[]

    // tag::uploadFeaturedImage[]
    def uploadFeaturedImage(FeaturedImageCommand cmd) {
        if (cmd.hasErrors()) {
            respond cmd.errors, model: [hotel: cmd], view: 'editFeaturedImage'
            return
        }
        Hotel hotel
        try {
            hotel = uploadHotelFeaturedImageService.uploadFeaturedImage(cmd) // <1>
        } catch (SdkException e) { // <2>
            log.error('Could not upload the featured image of hotel {} to S3', cmd.id, e)
            cmd.errors.reject('hotel.featuredImage.upload.failed',
                    'The featured image could not be uploaded. Try again later.')
            respond cmd.errors, model: [hotel: cmd], view: 'editFeaturedImage'
            return
        } catch (OptimisticLockingFailureException e) { // <3>
            flash.message = optimisticLockingFailureMessage()
            redirect action: 'editFeaturedImage', id: cmd.id
            return
        }
        if (hotel == null) {
            notFound()
            return
        }
        if (hotel.hasErrors()) {
            respond hotel.errors, model: [hotel: hotel], view: 'editFeaturedImage'
            return
        }
        flash.message = crudMessage(CRUD.UPDATE, hotel.id)
        redirect hotel
    }
    // end::uploadFeaturedImage[]

    // tag::delete[]
    def delete(Long id) {
        if (id == null) {
            notFound()
            return
        }
        String featuredImageKey = hotelDataService.get(id)?.featuredImageKey
        hotelDataService.delete(id)
        if (featuredImageKey) {
            uploadHotelFeaturedImageService.deleteFileByKey(featuredImageKey) // <1>
        }
        flash.message = crudMessage(CRUD.DELETE, id)
        redirect action: 'index', method: 'GET'
    }
    // end::delete[]

    protected void notFound() {
        flash.message = crudMessage(CRUD.NOT_FOUND, params.long('id'))
        redirect action: 'index', method: 'GET'
    }

    // tag::messages[]
    private String crudMessage(CRUD crud, Long id) {
        crudMessageService.message(crud, domainName(), id, request.locale)
    }

    private String optimisticLockingFailureMessage() {
        messageSource.getMessage('default.optimistic.locking.failure', [domainName()] as Object[],
                'Another user has updated this {0} while you were editing', request.locale)
    }

    private String domainName() {
        messageSource.getMessage('hotel.label', [] as Object[], 'Hotel', request.locale)
    }
    // end::messages[]
}
