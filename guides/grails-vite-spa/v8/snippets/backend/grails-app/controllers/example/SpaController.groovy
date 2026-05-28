package example

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader

class SpaController {
    static responseFormats = ['html']

    @Autowired
    ResourceLoader resourceLoader

    /**
     * Serves the SPA's bundled index.html (which Vite produced into
     * src/main/resources/public/index.html and the build copied onto the
     * classpath). Reading the classpath resource directly is robust across
     * profiles: a client-side route like /books/42 that hard-reloads to '/'
     * always gets the SPA shell, which then re-resolves the route in the
     * browser.
     */
    def index() {
        Resource indexHtml = resourceLoader.getResource('classpath:public/index.html')
        render(text: indexHtml.inputStream.getText('UTF-8'), contentType: 'text/html')
    }
}
