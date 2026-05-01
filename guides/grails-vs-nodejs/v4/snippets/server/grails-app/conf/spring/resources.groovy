import demo.UserPasswordEncoderListener
import demo.SessionExpirationJobHolder
import demo.TokenCreationEventListener
import demo.CustomWebSocketConfig

// Place your Spring DSL code here
beans = {
    userPasswordEncoderListener(UserPasswordEncoderListener) //<1>
    webSocketConfig(CustomWebSocketConfig)                 //<2>
    tokenCreationEventListener(TokenCreationEventListener) //<3>
    sessionExpirationJobHolder(SessionExpirationJobHolder) //<4>
}
