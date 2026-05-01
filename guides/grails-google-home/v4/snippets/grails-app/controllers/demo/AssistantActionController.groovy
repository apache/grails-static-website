// tag::packageImport[]
package demo

// end::packageImport[]

// tag::importStatements[]
import com.frogermcs.gactions.AssistantActions
import com.frogermcs.gactions.api.StandardIntents
import com.frogermcs.gactions.api.request.RootRequest
import com.google.gson.Gson
import com.google.gson.stream.JsonReader
import javax.servlet.http.HttpServletRequest
import groovy.transform.CompileStatic

// end::importStatements[]

// tag::classDeclaration[]
@CompileStatic
class AssistantActionController {
// end::classDeclaration[]

    // tag::index[]
    def index() {
        AssistantActions assistantActions =
                new AssistantActions.Builder(new GrailsResponseHandler(response))
                        .addRequestHandlerFactory(StandardIntents.MAIN, new MainRequestHandlerFactory())  // <1>
                        .addRequestHandlerFactory(StandardIntents.TEXT, new TextRequestHandlerFactory())
                        .build()
        RootRequest rootRequest = parseActionRequest(request)
        assistantActions.handleRequest(rootRequest) // <2>
        null // <3>
    }
    // end::index[]

    // tag::color[]
    def color() {
        AssistantActions assistantActions =
                new AssistantActions.Builder(new GrailsResponseHandler(response))
                        .addRequestHandlerFactory('color.intent', new ColorRequestHandlerFactory()) // <1>
                        .build()
        RootRequest rootRequest = parseActionRequest(request)  // <2>
        assistantActions.handleRequest(rootRequest)
        null  // <3>
    }
    // end::color[]

    // tag::parseActionRequest[]
    private RootRequest parseActionRequest(HttpServletRequest request) throws IOException {
        JsonReader jsonReader = new JsonReader(request.reader)
        new Gson().fromJson(jsonReader, RootRequest)
    }
    // end::parseActionRequest[]
}
