package com.ociweb.quickcasts

import grails.config.Config
import grails.core.support.GrailsConfigurationAware
import grails.transaction.Transactional

@Transactional
class RelatedQuickcastsService implements GrailsConfigurationAware {

    int numberOfRelatedQuickcasts

    @Override
    void setConfiguration(Config co) {
        numberOfRelatedQuickcasts = co.getRequiredProperty('ociweb.quickcasts.numberOfRelatedQuickcasts', Integer)
    }

    List<Quickcast> findAllRelatedQuickcasts(Quickcast quickcast) {
        def criteria = Quickcast.createCriteria()
        criteria.list {
            ne('id', quickcast?.id)
            maxResults(numberOfRelatedQuickcasts)
        } as List<Quickcast>
    }
}
