package com.ociweb.quickcasts

import grails.gorm.services.Service

@Service(Quickcast)
interface QuickcastService {
    List<Quickcast> findAll()
    Quickcast findById(Long id)
    Quickcast save(Quickcast quickcast)
}