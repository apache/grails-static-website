package example

import example.events.OrderPlacedEvent
import org.springframework.scheduling.annotation.Async
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

/**
 * Off-thread side-effect: stand-in for sending a confirmation email.
 *
 * An ordinary Grails service, auto-registered by name - which also means Grails
 * injects a `log` (SLF4J) property for free, so the class declares none of its
 * own. @Async dispatches the listener body to Spring's task executor instead of
 * running it on the caller's thread. The transaction-phase binding is still
 * synchronous - Spring waits for AFTER_COMMIT, then hands the event to the
 * executor. The body therefore has NO inherited transaction; if it needs the
 * database it must open its own via withNewTransaction { }.
 *
 * Exceptions thrown from an @Async listener are NOT propagated to the
 * publisher - see Spring's AsyncUncaughtExceptionHandler.
 *
 * @EnableAsync must be present on Application for @Async to take effect;
 * without it the annotation is silently a no-op.
 */
class NotificationService {

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    void onOrderPlaced(OrderPlacedEvent event) {
        log.info('Off-thread: sending confirmation email for order {} (total {})',
                event.orderId, event.total)
        // Real implementation would call emailService.send(...)
    }
}
