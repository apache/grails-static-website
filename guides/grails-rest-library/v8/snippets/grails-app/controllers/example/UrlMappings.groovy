package example

class UrlMappings {

    static mappings = {

        '/v1/books'(resources: 'book') {
            collection {
                '/bulk'(controller: 'book', action: 'bulkCreate', method: 'POST')
            }
        }
        '/v1/authors'(resources: 'author')

        '/'(view: '/index')

        '500'(view: '/error')
        '404'(view: '/notFound')
    }
}
