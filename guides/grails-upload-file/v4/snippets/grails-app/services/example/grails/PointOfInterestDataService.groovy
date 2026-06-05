package example.grails

import grails.gorm.services.Service

@SuppressWarnings(['LineLength', 'UnusedVariable', 'SpaceAfterOpeningBrace', 'SpaceBeforeClosingBrace'])
@Service(PointOfInterest)
interface PointOfInterestDataService {

    PointOfInterest get(Long id)

    List<PointOfInterest> list(Map args)

    Number count()

    void delete(Serializable id)

    PointOfInterest save(String name)

    PointOfInterest updateName(Serializable id, Long version, String name)

    PointOfInterest updateFeaturedImageUrl(Serializable id, Long version, String featuredImageUrl)
}
