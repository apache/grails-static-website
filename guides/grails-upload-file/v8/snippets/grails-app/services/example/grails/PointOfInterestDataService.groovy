package example.grails

import grails.gorm.services.Service
import grails.gorm.transactions.Transactional

@Service(PointOfInterest)
abstract class PointOfInterestDataService {

    abstract PointOfInterest get(Serializable id)

    abstract List<PointOfInterest> list(Map args)

    abstract Number count()

    abstract void delete(Serializable id)

    abstract PointOfInterest save(String name)

    abstract PointOfInterest updateName(Serializable id, Long version, String name)

    @Transactional
    PointOfInterest updateFeaturedImageUrl(Serializable id, Long version, String featuredImageUrl) { // <1>
        PointOfInterest poi = PointOfInterest.get(id)
        if (!poi) {
            return null
        }
        if (version != null && poi.version > version) { // <2>
            poi.errors.rejectValue('version', 'default.optimistic.locking.failure',
                    ['Point of Interest'] as Object[],
                    'Another user has updated this Point of Interest while you were editing')
            return poi
        }
        poi.featuredImageUrl = featuredImageUrl
        poi.save()
        poi
    }
}
