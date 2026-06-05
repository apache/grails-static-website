package example.grails

import groovy.transform.CompileStatic
import org.springframework.context.MessageSource

@SuppressWarnings('LineLength')
@CompileStatic
class RestaurantController {

    static allowedMethods = [
            save: 'POST',
            update: 'PUT',
            uploadFeaturedImage: 'POST',
            delete: 'DELETE',
    ]

    RestaurantDataService restaurantDataService

    MessageSource messageSource

    CrudMessageService crudMessageService

    private String domainName(Locale locale) {
        messageSource.getMessage('restaurant.label',
                [] as Object[],
                'Restaurant',
                locale)
    }

    // tag::featuredImage[]
    def featuredImage(Long id) {
        Restaurant restaurant = restaurantDataService.get(id)
        if (!restaurant || restaurant.featuredImageBytes == null) {
            notFound()
            return
        }
        render file: restaurant.featuredImageBytes,
            contentType: restaurant.featuredImageContentType
    }
    // end::featuredImage[]

    // tag::editFeaturedImage[]
    def editFeaturedImage(Long id) {
        Restaurant restaurant = restaurantDataService.get(id)
        if (!restaurant) {
            notFound()
        }
        [restaurant: restaurant]
    }
    // end::editFeaturedImage[]

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        [
                restaurantList: restaurantDataService.list(params),
                restaurantCount: restaurantDataService.count(),
        ]
    }

    def show(Long id) {
        Restaurant restaurant = restaurantDataService.get(id)
        if (!restaurant) {
            notFound()
        }
        [restaurant: restaurant]
    }

    @SuppressWarnings(['FactoryMethodName'])
    def create() {
        [restaurant: new Restaurant()]
    }

    def edit(Long id) {
        Restaurant restaurant = restaurantDataService.get(id)
        if (!restaurant) {
            notFound()
        }
        [restaurant: restaurant]
    }

    // tag::uploadFeaturedImage[]
    def uploadFeaturedImage(FeaturedImageCommand cmd) {
        if (cmd == null) {
            notFound()
            return
        }

        if (cmd.hasErrors()) {
            respond(cmd.errors, model: [restaurant: cmd], view: 'editFeaturedImage')
            return
        }

        Restaurant restaurant = restaurantDataService.update(cmd.id,
                cmd.version,
                cmd.featuredImageFile.bytes,
                cmd.featuredImageFile.contentType)

        if (restaurant == null) {
            notFound()
            return
        }

        if (restaurant.hasErrors()) {
            respond(restaurant.errors, model: [restaurant: restaurant], view: 'editFeaturedImage')
            return
        }

        Locale locale = request.locale
        flash.message = crudMessageService.message(CRUD.UPDATE, domainName(locale), cmd.id, locale)
        redirect restaurant
    }
    // end::uploadFeaturedImage[]

    def save(NameCommand cmd) {
        if (cmd == null) {
            notFound()
            return
        }

        if (cmd.hasErrors()) {
            respond cmd.errors, model: [restaurant: cmd], view:'create'
            return
        }

        Restaurant restaurant = restaurantDataService.save(cmd.name)

        if (restaurant == null) {
            notFound()
            return
        }

        if (restaurant.hasErrors()) {
            respond restaurant.errors, model: [restaurant: restaurant], view:'create'
            return
        }

        Locale locale = request.locale
        flash.message = crudMessageService.message(CRUD.CREATE, domainName(locale), restaurant.id, locale)
        redirect restaurant
    }

    def update(NameUpdateCommand cmd) {
        if (cmd == null) {
            notFound()
            return
        }

        if (cmd.hasErrors()) {
            respond cmd.errors, model: [restaurant: cmd], view:'edit'
            return
        }

        Restaurant restaurant = restaurantDataService.update(cmd.id, cmd.version, cmd.name)

        if (restaurant == null) {
            notFound()
            return
        }

        if (restaurant.hasErrors()) {
            respond restaurant.errors, model: [restaurant: restaurant], view:'edit'
            return
        }

        Locale locale = request.locale
        flash.message = crudMessageService.message(CRUD.UPDATE, domainName(locale), cmd.id, locale)
        redirect restaurant
    }

    def delete(Long id) {

        if (id == null) {
            notFound()
            return
        }

        restaurantDataService.delete(id)

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
