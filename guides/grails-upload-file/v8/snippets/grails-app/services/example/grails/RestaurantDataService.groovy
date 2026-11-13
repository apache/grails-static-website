package example.grails

import grails.gorm.services.Service

@Service(Restaurant)
interface RestaurantDataService {

    Restaurant get(Serializable id)

    List<Restaurant> list(Map args)

    Number count()

    void delete(Serializable id)

    Restaurant save(String name)

    Restaurant update(Serializable id, Long version, String name)

    Restaurant update(Serializable id, Long version, byte[] featuredImageBytes, String featuredImageContentType) // <1>
}
