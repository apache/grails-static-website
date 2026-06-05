package demo

import com.frogermcs.gactions.api.RequestHandler
import com.frogermcs.gactions.api.request.RootRequest
import groovy.transform.CompileStatic

@CompileStatic
class MainRequestHandlerFactory extends RequestHandler.Factory {
    @Override
    RequestHandler create(RootRequest rootRequest) {
        new MainRequestHandler(rootRequest)
    }
}
