package example.grails

import grails.gorm.services.Service
import grails.gorm.transactions.Transactional

@Service(Hotel)
abstract class HotelDataService {

    abstract Hotel get(Serializable id)

    abstract List<Hotel> list(Map args)

    abstract Number count()

    abstract void delete(Serializable id)

    abstract Hotel save(String name)

    abstract Hotel update(Serializable id, Long version, String name)

    @Transactional
    Hotel update(Serializable id, Long version, String featuredImageKey, String featuredImageUrl) { // <1>
        Hotel hotel = Hotel.get(id)
        if (!hotel) {
            return null
        }
        if (version != null && hotel.version > version) { // <2>
            hotel.errors.rejectValue('version', 'default.optimistic.locking.failure',
                    ['Hotel'] as Object[],
                    'Another user has updated this Hotel while you were editing')
            return hotel
        }
        hotel.featuredImageKey = featuredImageKey
        hotel.featuredImageUrl = featuredImageUrl
        hotel.save()
        hotel
    }
}
