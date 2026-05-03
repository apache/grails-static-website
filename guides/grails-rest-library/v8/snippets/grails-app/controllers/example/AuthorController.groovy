package example

import grails.rest.RestfulController

class AuthorController extends RestfulController<Author> {

    static responseFormats = ['json']

    AuthorController() { super(Author) }

    /**
     * GET /v1/authors?max=20&offset=0&sort=name&order=asc
     *
     * Bound `max` defaults to 25 and is hard-capped at 100 so a paginating
     * client cannot ask for the full table in one request. `offset`,
     * `sort`, and `order` flow through to the GORM query unchanged.
     */
    @Override
    protected List<Author> listAllResources(Map params) {
        params.max = Math.min(params.int('max', 25), 100)
        Author.list(params)
    }
}
