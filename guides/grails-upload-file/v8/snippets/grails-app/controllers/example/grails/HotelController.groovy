package example.grails

import grails.compiler.GrailsCompileStatic
import org.springframework.context.MessageSource

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
        Hotel hotel = hotelDataService.update(cmd.id, cmd.version, cmd.name)
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
        Hotel hotel = uploadHotelFeaturedImageService.uploadFeatureImage(cmd) // <1>
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

    def delete(Long id) {
        if (id == null) {
            notFound()
            return
        }
        hotelDataService.delete(id)
        flash.message = crudMessage(CRUD.DELETE, id)
        redirect action: 'index', method: 'GET'
    }

    protected void notFound() {
        flash.message = crudMessage(CRUD.NOT_FOUND, params.long('id'))
        redirect action: 'index', method: 'GET'
    }

    private String crudMessage(CRUD crud, Long id) {
        Locale locale = request.locale
        String domainName = messageSource.getMessage('hotel.label', [] as Object[], 'Hotel', locale)
        crudMessageService.message(crud, domainName, id, locale)
    }
}
