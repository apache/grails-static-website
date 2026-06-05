package demo

import java.io.InputStream
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.UUID
import java.util.regex.Matcher
import java.util.regex.Pattern

class AiDataController {

    private static final int CONNECT_TIMEOUT_MILLIS = 5000
    private static final int READ_TIMEOUT_MILLIS = 10000
    private static final int MAX_QUERY_LENGTH = 1024
    private static final String API_AI_SESSION_ID_ATTRIBUTE = 'apiAiSessionId'
    private static final Pattern SPEECH_PATTERN = Pattern.compile('"speech"\\s*:\\s*"((?:\\\\.|[^"\\\\])*)"')

    def index() {
        String query = request.getParameter('query')
        if (!query) {
            response.setStatus(400)
            response.setContentType('text/plain')
            response.writer.append('Missing required query parameter: query')
            return null
        }
        if (query.length() > MAX_QUERY_LENGTH) {
            response.setStatus(400)
            response.setContentType('text/plain')
            response.writer.append('Query parameter exceeds maximum length')
            return null
        }

        String apiAiKey = grailsApplication.config.getProperty('apiAiKey', String)
        if (!apiAiKey || apiAiKey.startsWith('<')) {
            response.setStatus(500)
            response.setContentType('text/plain')
            response.writer.append('API.AI key is not configured')
            return null
        }

        try {
            String speech = requestApiAi(query, request.getSession(), apiAiKey)
            response.setContentType('text/plain')
            response.writer.append(speech ?: '')
        } catch (IOException e) {
            response.setStatus(502)
            response.setContentType('text/plain')
            response.writer.append('')
        }
        null
    }

    private String requestApiAi(String query, def session, String apiAiKey) {
        HttpURLConnection connection = (HttpURLConnection) new URL('https://api.api.ai/v1/query?v=20150910').openConnection()
        connection.requestMethod = 'POST'
        connection.doOutput = true
        connection.connectTimeout = CONNECT_TIMEOUT_MILLIS
        connection.readTimeout = READ_TIMEOUT_MILLIS
        connection.setRequestProperty('Accept', 'application/json')
        connection.setRequestProperty('Authorization', "Bearer ${apiAiKey}")
        connection.setRequestProperty('Content-Type', 'application/json; charset=UTF-8')

        String sessionId = conversationSessionId(session)
        String payload = "{\"lang\":\"en\",\"query\":[\"${escapeJson(query)}\"],\"sessionId\":\"${escapeJson(sessionId)}\"}"

        connection.outputStream.withCloseable { OutputStream outputStream ->
            outputStream.write(payload.getBytes(StandardCharsets.UTF_8))
        }

        int responseCode = connection.responseCode
        InputStream inputStream = responseCode >= 400 ? connection.errorStream : connection.inputStream
        if (responseCode >= 400) {
            if (inputStream) {
                inputStream.close()
            }
            throw new IOException("API.AI returned status ${responseCode}")
        }
        if (!inputStream) {
            return ''
        }

        try {
            extractSpeech(inputStream.getText(StandardCharsets.UTF_8.name()))
        } finally {
            inputStream.close()
            connection.disconnect()
        }
    }

    private static String conversationSessionId(def session) {
        String sessionId = (String) session?.getAttribute(API_AI_SESSION_ID_ATTRIBUTE)
        if (!sessionId) {
            sessionId = UUID.randomUUID().toString()
            session?.setAttribute(API_AI_SESSION_ID_ATTRIBUTE, sessionId)
        }
        sessionId
    }

    private String extractSpeech(String body) {
        Matcher matcher = SPEECH_PATTERN.matcher(body ?: '')
        matcher.find() ? unescapeJson(matcher.group(1)) : ''
    }

    private static String escapeJson(String value) {
        StringBuilder escaped = new StringBuilder()
        for (int i = 0; i < value.length(); i++) {
            char character = value.charAt(i)
            switch (character) {
                case '"':
                    escaped.append('\\"')
                    break
                case '\\':
                    escaped.append('\\\\')
                    break
                case '\b':
                    escaped.append('\\b')
                    break
                case '\f':
                    escaped.append('\\f')
                    break
                case '\n':
                    escaped.append('\\n')
                    break
                case '\r':
                    escaped.append('\\r')
                    break
                case '\t':
                    escaped.append('\\t')
                    break
                default:
                    if (character < 0x20) {
                        escaped.append(String.format('\\u%04x', (int) character))
                    } else {
                        escaped.append(character)
                    }
            }
        }
        escaped.toString()
    }

    private static String unescapeJson(String value) {
        StringBuilder unescaped = new StringBuilder()
        int index = 0
        while (index < value.length()) {
            char character = value.charAt(index)
            if (character == '\\' && index + 1 < value.length()) {
                char escaped = value.charAt(index + 1)
                switch (escaped) {
                    case '"':
                    case '\\':
                    case '/':
                        unescaped.append(escaped)
                        index += 2
                        continue
                    case 'b':
                        unescaped.append('\b')
                        index += 2
                        continue
                    case 'f':
                        unescaped.append('\f')
                        index += 2
                        continue
                    case 'n':
                        unescaped.append('\n')
                        index += 2
                        continue
                    case 'r':
                        unescaped.append('\r')
                        index += 2
                        continue
                    case 't':
                        unescaped.append('\t')
                        index += 2
                        continue
                    case 'u':
                        if (index + 5 < value.length()) {
                            String hex = value.substring(index + 2, index + 6)
                            unescaped.append((char) Integer.parseInt(hex, 16))
                            index += 6
                            continue
                        }
                }
            }
            unescaped.append(character)
            index++
        }
        unescaped.toString()
    }
}
