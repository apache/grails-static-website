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

    @Transactional
    Restaurant update(Serializable id, Long version, byte[] featuredImageBytes, String featuredImageContentType) { // <1>
        Restaurant restaurant = Restaurant.get(id)
        if (!restaurant || StaleVersion.reject(restaurant.errors, restaurant.version, version, 'Restaurant')) { // <2>
            return restaurant
        }
        restaurant.featuredImageBytes = featuredImageBytes
        restaurant.featuredImageContentType = featuredImageContentType
        restaurant.save()
        restaurant
    }

    @Transactional
    Restaurant update(Serializable id, Long version, String name) { // <3>
        Restaurant restaurant = Restaurant.get(id)
        if (!restaurant || StaleVersion.reject(restaurant.errors, restaurant.version, version, 'Restaurant')) {
            return restaurant
        }
        restaurant.name = name
        restaurant.save()
        restaurant
    }
}
