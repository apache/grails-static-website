package example.grails

import grails.gorm.services.Service

@Service(Hotel)
interface HotelDataService {
    Hotel get(Long id)
    List<Hotel> list(Map args)
    Number count()
    void delete(Serializable id)
    Hotel save(String name)
    Hotel update(Serializable id, Long version, String name)
    Hotel update(Serializable id, Long version, String featuredImageKey, String featuredImageUrl)
}
