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

    @Transactional
    Hotel update(Serializable id, Long version, String featuredImageKey, String featuredImageUrl) { // <1>
        Hotel hotel = Hotel.get(id)
        if (!hotel || StaleVersion.reject(hotel.errors, hotel.version, version, 'Hotel')) { // <2>
            return hotel
        }
        hotel.featuredImageKey = featuredImageKey
        hotel.featuredImageUrl = featuredImageUrl
        hotel.save()
        hotel
    }

    @Transactional
    Hotel update(Serializable id, Long version, String name) { // <3>
        Hotel hotel = Hotel.get(id)
        if (!hotel || StaleVersion.reject(hotel.errors, hotel.version, version, 'Hotel')) {
            return hotel
        }
        hotel.name = name
        hotel.save()
        hotel
    }
}
