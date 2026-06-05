package com.ociweb.quickcasts

import groovy.transform.CompileStatic

@CompileStatic
class QuickcastController {
    QuickcastService quickcastService
    static responseFormats = ['xml']

    RelatedQuickcastsService relatedQuickcastsService

    def index() {
        [quickcasts: quickcastService.findAll()]
    }

    def show(Long id) {
        Quickcast quickcast = quickcastService.findById(id)
        [
                quickcast: quickcast,
                relatedQuickcasts: relatedQuickcastsService.findAllRelatedQuickcasts(quickcast)
        ]
    }
}
