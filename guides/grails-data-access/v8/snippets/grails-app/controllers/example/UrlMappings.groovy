package example

class UrlMappings {

    static mappings = {
        "/$controller/$action?/$id?(.$format)?"{
            constraints {
                // apply constraints here
            }
        }

        "/api/books/byAuthor/$authorId"(controller: 'book', action: 'byAuthor')
        "/api/books/search"(controller: 'book', action: 'search')
        "/api/books"(resources: 'book')
        "/api/authors"(resources: 'author')

        "/"(controller: 'application', action: 'index')
        "500"(view: '/error')
        "404"(view: '/notFound')
    }
}
