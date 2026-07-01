package example

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader
import org.springframework.http.HttpStatus

/**
 * Serves the Vite-built SPA bundle (the hashed /assets/** files Vite emits
 * and copyFrontendToBackend places on the classpath under public/assets/).
 *
 * The rest-api Grails profile registers no servlet static-resource handler,
 * so these assets would 404 without an explicit route. Streaming them through
 * a controller keeps everything same-origin: the SPA shell at '/' and its JS
 * and CSS at '/assets/**' are all served by this one application.
 */
class AssetController {

    @Autowired
    ResourceLoader resourceLoader

    def serve() {
        String path = params.path
        if (!path || path.contains('..')) {
            response.status = HttpStatus.BAD_REQUEST.value()
            return
        }
        Resource asset = resourceLoader.getResource("classpath:public/assets/${path}")
        if (!asset.exists()) {
            response.status = HttpStatus.NOT_FOUND.value()
            return
        }
        response.contentType = contentTypeFor(path)
        asset.inputStream.withStream { input ->
            response.outputStream << input
        }
        response.outputStream.flush()
    }

    private static String contentTypeFor(String path) {
        if (path.endsWith('.js'))   return 'text/javascript'
        if (path.endsWith('.css'))  return 'text/css'
        if (path.endsWith('.svg'))  return 'image/svg+xml'
        if (path.endsWith('.json')) return 'application/json'
        'application/octet-stream'
    }
}
