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

    @Transactional
    PointOfInterest updateFeaturedImage(Serializable id, Long version, String featuredImageUrl) { // <1>
        PointOfInterest poi = PointOfInterest.get(id)
        if (!poi || StaleVersion.reject(poi.errors, poi.version, version, 'Point of Interest')) { // <2>
            return poi
        }
        poi.featuredImageUrl = featuredImageUrl
        poi.save()
        poi
    }

    @Transactional
    PointOfInterest updateName(Serializable id, Long version, String name) { // <3>
        PointOfInterest poi = PointOfInterest.get(id)
        if (!poi || StaleVersion.reject(poi.errors, poi.version, version, 'Point of Interest')) {
            return poi
        }
        poi.name = name
        poi.save()
        poi
    }
}
