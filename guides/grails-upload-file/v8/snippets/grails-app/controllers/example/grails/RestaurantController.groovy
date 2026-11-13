package example.grails

import grails.compiler.GrailsCompileStatic
import org.springframework.context.MessageSource

@GrailsCompileStatic
class RestaurantController {

    static allowedMethods = [
            save: 'POST',
            update: 'PUT',
            uploadFeaturedImage: 'POST',
            delete: 'DELETE',
    ]

    RestaurantDataService restaurantDataService
    CrudMessageService crudMessageService
    MessageSource messageSource

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
            return
        }
        [restaurant: restaurant]
    }

    // tag::featuredImage[]
    def featuredImage(Long id) {
        Restaurant restaurant = restaurantDataService.get(id)
        if (!restaurant || restaurant.featuredImageBytes == null) {
            notFound()
            return
        }
        render file: restaurant.featuredImageBytes, contentType: restaurant.featuredImageContentType // <1>
    }
    // end::featuredImage[]

    def create() {
        [restaurant: new Restaurant()]
    }

    def save(NameCommand cmd) {
        if (cmd.hasErrors()) {
            respond cmd.errors, model: [restaurant: cmd], view: 'create'
            return
        }
        Restaurant restaurant = restaurantDataService.save(cmd.name)
        if (restaurant.hasErrors()) {
            respond restaurant.errors, model: [restaurant: restaurant], view: 'create'
            return
        }
        flash.message = crudMessage(CRUD.CREATE, restaurant.id)
        redirect restaurant
    }

    def edit(Long id) {
        Restaurant restaurant = restaurantDataService.get(id)
        if (!restaurant) {
            notFound()
            return
        }
        [restaurant: restaurant]
    }

    def update(NameUpdateCommand cmd) {
        if (cmd.hasErrors()) {
            respond cmd.errors, model: [restaurant: cmd], view: 'edit'
            return
        }
        Restaurant restaurant = restaurantDataService.update(cmd.id, cmd.version, cmd.name)
        if (restaurant == null) {
            notFound()
            return
        }
        if (restaurant.hasErrors()) {
            respond restaurant.errors, model: [restaurant: restaurant], view: 'edit'
            return
        }
        flash.message = crudMessage(CRUD.UPDATE, restaurant.id)
        redirect restaurant
    }

    // tag::editFeaturedImage[]
    def editFeaturedImage(Long id) {
        Restaurant restaurant = restaurantDataService.get(id)
        if (!restaurant) {
            notFound()
            return
        }
        [restaurant: restaurant]
    }
    // end::editFeaturedImage[]

    // tag::uploadFeaturedImage[]
    def uploadFeaturedImage(FeaturedImageCommand cmd) {
        if (cmd.hasErrors()) { // <1>
            respond cmd.errors, model: [restaurant: cmd], view: 'editFeaturedImage'
            return
        }
        Restaurant restaurant = restaurantDataService.update(cmd.id,
                cmd.version,
                cmd.featuredImageFile.bytes, // <2>
                cmd.featuredImageFile.contentType)
        if (restaurant == null) {
            notFound()
            return
        }
        if (restaurant.hasErrors()) {
            respond restaurant.errors, model: [restaurant: restaurant], view: 'editFeaturedImage'
            return
        }
        flash.message = crudMessage(CRUD.UPDATE, restaurant.id)
        redirect restaurant
    }
    // end::uploadFeaturedImage[]

    def delete(Long id) {
        if (id == null) {
            notFound()
            return
        }
        restaurantDataService.delete(id)
        flash.message = crudMessage(CRUD.DELETE, id)
        redirect action: 'index', method: 'GET'
    }

    protected void notFound() {
        flash.message = crudMessage(CRUD.NOT_FOUND, params.long('id'))
        redirect action: 'index', method: 'GET'
    }

    private String crudMessage(CRUD crud, Long id) {
        Locale locale = request.locale
        String domainName = messageSource.getMessage('restaurant.label', [] as Object[], 'Restaurant', locale)
        crudMessageService.message(crud, domainName, id, locale)
    }
}
