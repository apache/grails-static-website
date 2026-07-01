package example.grails

import org.springframework.context.MessageSource
import groovy.transform.CompileStatic

@SuppressWarnings('LineLength')
@CompileStatic
class HotelController {

    static allowedMethods = [
            save: 'POST',
            update: 'PUT',
            uploadFeaturedImage: 'POST',
            delete: 'DELETE',
    ]

    UploadHotelFeaturedImageService uploadHotelFeaturedImageService

    HotelDataService hotelDataService

    MessageSource messageSource

    CrudMessageService crudMessageService

    private String domainName(Locale locale) {
        messageSource.getMessage('hotel.label',
                [] as Object[],
                'Hotel',
                locale)
    }

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

    @SuppressWarnings(['FactoryMethodName'])
    def create() {
        [hotel: new Hotel()]
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

    def edit(Long id) {
        Hotel hotel = hotelDataService.get(id)
        if (!hotel) {
            notFound()
            return
        }
        [hotel: hotel]
    }

    // tag::uploadFeaturedImage[]
    def uploadFeaturedImage(FeaturedImageCommand cmd) {

        if (cmd.hasErrors()) {
            respond(cmd.errors, model: [hotel: cmd], view: 'editFeaturedImage')
            return
        }

        def hotel = uploadHotelFeaturedImageService.uploadFeatureImage(cmd)
        if (hotel == null) {
            notFound()
            return
        }

        if (hotel.hasErrors()) {
            respond(hotel.errors, model: [hotel: hotel], view: 'editFeaturedImage')
            return
        }

        Locale locale = request.locale
        flash.message = crudMessageService.message(CRUD.UPDATE, domainName(locale), hotel.id, locale)
        redirect hotel
    }
    // end::uploadFeaturedImage[]

    def save(NameCommand cmd) {
        if (cmd == null) {
            notFound()
            return
        }

        if (cmd.hasErrors()) {
            respond cmd.errors, model: [hotel: cmd], view:'create'
            return
        }

        Hotel hotel = hotelDataService.save(cmd.name)

        if ( hotel == null ) {
            notFound()
            return
        }

        if ( hotel.hasErrors() ) {
            respond hotel.errors, model: [hotel: hotel], view:'create'
            return
        }

        Locale locale = request.locale
        flash.message = crudMessageService.message(CRUD.CREATE, domainName(locale), hotel.id, locale)
        redirect hotel
    }

    def update(NameUpdateCommand cmd) {
        if (cmd == null) {
            notFound()
            return
        }

        if (cmd.hasErrors()) {
            respond cmd.errors, model: [hotel: cmd], view: 'edit'
            return
        }

        Hotel hotel = hotelDataService.update(cmd.id, cmd.version, cmd.name)

        if ( hotel == null) {
            notFound()
            return
        }

        if ( hotel.hasErrors() ) {
            respond hotel.errors, model: [hotel: hotel], view: 'edit'
            return
        }

        Locale locale = request.locale
        flash.message = crudMessageService.message(CRUD.UPDATE, domainName(locale), hotel.id, locale)
        redirect hotel
    }

    def delete(Long id) {

        if (!id) {
            notFound()
            return
        }

        hotelDataService.delete(id)

        Locale locale = request.locale
        flash.message = crudMessageService.message(CRUD.DELETE, domainName(locale), id, locale)
        redirect action: 'index', method: 'GET'
    }

    protected void notFound() {
        Locale locale = request.locale
        flash.message = crudMessageService.message(CRUD.DELETE, domainName(locale), params.long('id'), locale)
        redirect action: 'index', method: 'GET'
    }
}
