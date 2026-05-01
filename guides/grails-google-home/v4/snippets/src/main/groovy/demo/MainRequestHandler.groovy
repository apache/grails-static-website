package demo

import com.frogermcs.gactions.ResponseBuilder
import com.frogermcs.gactions.api.RequestHandler
import com.frogermcs.gactions.api.request.RootRequest
import com.frogermcs.gactions.api.response.RootResponse
import groovy.transform.CompileStatic

@CompileStatic
class MainRequestHandler extends RequestHandler {
    protected MainRequestHandler(RootRequest rootRequest) {
        super(rootRequest)
    }

    @Override
    RootResponse getResponse() {
        ResponseBuilder.askResponse('Hey, it works! Now tell something so I could repeat it.')
    }
}
