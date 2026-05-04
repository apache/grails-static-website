package example

import grails.rest.RestfulController

class AuthorController extends RestfulController<Author> {

    static responseFormats = ['json']

    AuthorController() {
        super(Author)
    }

    @Override
    def index(Integer max) {
        if (max != null && max < 0) {
            max = null
        }

        params.max = Math.min(max ?: 25, 100)
        params.offset = Math.max(params.int('offset', 0), 0)

        respond listAllResources(params), model: [authorCount: countResources()]
    }
}
