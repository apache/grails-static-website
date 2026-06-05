package demo

import grails.events.annotation.Publisher
import grails.gorm.transactions.Transactional
import grails.validation.ValidationException
import groovy.transform.CompileStatic
import org.springframework.context.MessageSource
import org.springframework.dao.DuplicateKeyException

@CompileStatic
class RegisterService {

    MessageSource messageSource

    @Transactional
    @Publisher // <1>
    User saveUser(User user) {
        user.save(failOnError: true) // <2>
        user
    }

    SaveUserResult register(RegisterCommand cmd, Locale locale) {
        User user = cmd as User
        try {
            saveUser(user)
            return new SaveUserResult(message: userSavedMessage(cmd.email, locale))
        } catch (ValidationException | DuplicateKeyException validationException) {
            return new SaveUserResult(error: userSavedErrorMessage(cmd.email, locale))
        }
    }

    String userSavedErrorMessage(String email, Locale locale) {
        messageSource.getMessage('user.saved.error',
                [email] as Object[],
                "Error while saving user with ${email} email address",
                locale)
    }

    String userSavedMessage(String email, Locale locale) {
        messageSource.getMessage('user.saved',
                [email] as Object[],
                "User saved with ${email} email address",
                locale)
    }
}

@CompileStatic
class SaveUserResult {
    String message
    String error
}
