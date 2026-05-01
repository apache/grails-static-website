package demo

import com.frogermcs.gactions.ResponseHandler
import com.frogermcs.gactions.api.response.RootResponse
import com.google.gson.Gson
import groovy.transform.CompileStatic
import javax.servlet.http.HttpServletResponse
import groovy.util.logging.Slf4j

/**
 * This is a handler for the SDK to make it work with Grails properly
 */
@CompileStatic
@Slf4j
class GrailsResponseHandler implements ResponseHandler {
    private final HttpServletResponse httpServletResponse
    private final Gson gson

    GrailsResponseHandler(HttpServletResponse httpServletResponse) {
        this(httpServletResponse, new Gson())
    }

    GrailsResponseHandler(HttpServletResponse httpServletResponse, Gson gson) {
        this.httpServletResponse = httpServletResponse
        this.gson = gson
    }

    @Override
    void onPrepareContentType(String contentType) {
        httpServletResponse.setContentType(contentType)
    }

    @Override
    void onPrepareResponseHeaders(Map<String, String> headers) {
        for (String headerName : headers.keySet()) {
            httpServletResponse.addHeader(headerName, headers.get(headerName))
        }
    }

    @Override
    void onResponse(RootResponse rootResponse) {
        try {
            gson.toJson(rootResponse, httpServletResponse.writer)
            httpServletResponse.flushBuffer()
        } catch (IOException e) {
            log.error('Error writing response', e)
        }
    }

    String getResponse(RootResponse rootResponse) {

        try {
            gson.toJson(rootResponse)
        } catch (IOException e) {
            log.error('Error getting response', e)
        }
    }
}
