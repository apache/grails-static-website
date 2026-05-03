package example

class SpaController {
    static responseFormats = ['html']

    /**
     * Forwards any non-/api request to the SPA's bundled index.html
     * (which Vite produced into src/main/resources/public/index.html).
     * Spring Boot auto-serves that file at /; we forward here so client-
     * side routes like /books/42 survive a browser refresh.
     */
    def index() {
        forward url: '/index.html'
    }
}
