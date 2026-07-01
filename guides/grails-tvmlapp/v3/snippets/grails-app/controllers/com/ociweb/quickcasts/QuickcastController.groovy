package com.ociweb.quickcasts

class QuickcastController {
    static responseFormats = ['xml']

    def relatedQuickcastsService

    def index() {
        [quickcasts: Quickcast.findAll()]
    }

    def show() {
        def quickcast = Quickcast.get(params.id)
        def relatedQuickcasts = relatedQuickcastsService.findAllRelatedQuickcasts(quickcast)
        [quickcast: quickcast, relatedQuickcasts: relatedQuickcasts]
    }
}
