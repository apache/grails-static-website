package example

class UrlMappings {

    static mappings = {

        // SPA routes resolved here so client-side routing survives
        // a hard reload to any deep link.
        '/' (controller: 'spa', action: 'index')

        // The Vite-built JS/CSS bundle, served same-origin from the classpath.
        "/assets/$path**"(controller: 'asset', action: 'serve')

        // JSON API surface consumed by the Vite/React frontend.
        '/api/books'(resources: 'book')

        '500'(view: '/error')
        '404'(view: '/notFound')
    }
}
