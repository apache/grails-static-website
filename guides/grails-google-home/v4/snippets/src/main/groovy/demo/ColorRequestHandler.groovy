package demo

import com.frogermcs.gactions.ResponseBuilder
import com.frogermcs.gactions.api.RequestHandler
import com.frogermcs.gactions.api.request.RootRequest
import com.frogermcs.gactions.api.response.RootResponse
import demo.util.ColorUtils
import java.awt.Color
import java.lang.reflect.Field
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

/**
 * take a color name input and returns a brighter color name in return if possible
 */
@CompileStatic
@Slf4j
class ColorRequestHandler extends RequestHandler {

    protected ColorRequestHandler(RootRequest rootRequest) {
        super(rootRequest)
    }

    @Override
    RootResponse getResponse() {
        log.debug("Inputs=${rootRequest.inputs.toListString()}")
        String color = rootRequest.inputs[0].arguments[0].raw_text.toLowerCase()

        Color parsedColor = null
        try {
            Field field = Class.forName('java.awt.Color').getField(color)
            parsedColor = (Color)field.get(null)
        } catch (NoSuchFieldException ne) {
            return colorNotFound(color)
        }
        if (parsedColor) {
            ColorUtils colorUtils = new ColorUtils()
            String brighter = colorUtils.findBrighterNameForColor(parsedColor).toLowerCase()
            String answer = "Sorry I can't find a brighter color for ${color}."
            if (brighter != color) {
                answer = "The brighter color for ${color} is ${brighter} "
            }
            return ResponseBuilder.tellResponse(answer)
        }
        colorNotFound(color)
    }

    private RootResponse colorNotFound(String color) {
        ResponseBuilder.tellResponse("Sorry I don't understand the color ${color}.")
    }
}
