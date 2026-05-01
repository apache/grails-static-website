package demo

import com.frogermcs.gactions.ResponseBuilder
import com.frogermcs.gactions.api.RequestHandler
import com.frogermcs.gactions.api.request.RootRequest
import com.frogermcs.gactions.api.response.RootResponse
import groovy.transform.CompileStatic

@CompileStatic
class TextRequestHandler extends RequestHandler {
    protected TextRequestHandler(RootRequest rootRequest) {
        super(rootRequest)
    }

    @Override
    RootResponse getResponse() {
        ResponseBuilder.tellResponse("You just told: ${rootRequest.inputs.get(0).raw_inputs.get(0).query}")
    }
}
