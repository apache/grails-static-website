package example.grails

import grails.gorm.services.Service
import grails.gorm.transactions.Transactional

@Service(Restaurant)
abstract class RestaurantDataService {

    abstract Restaurant get(Serializable id)

    abstract List<Restaurant> list(Map args)

    abstract Number count()

    abstract void delete(Serializable id)

    abstract Restaurant save(String name)

    abstract Restaurant update(Serializable id, Long version, String name)

    @Transactional
    Restaurant update(Serializable id, Long version, byte[] featuredImageBytes, String featuredImageContentType) { // <1>
        Restaurant restaurant = Restaurant.get(id)
        if (!restaurant) {
            return null
        }
        if (version != null && restaurant.version > version) { // <2>
            restaurant.errors.rejectValue('version', 'default.optimistic.locking.failure',
                    ['Restaurant'] as Object[],
                    'Another user has updated this Restaurant while you were editing')
            return restaurant
        }
        restaurant.featuredImageBytes = featuredImageBytes
        restaurant.featuredImageContentType = featuredImageContentType
        restaurant.save()
        restaurant
    }
}
