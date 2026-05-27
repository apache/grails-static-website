package example

import example.events.OrderPlacedEvent
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Async
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

/**
 * Off-thread side-effect: stand-in for sending a confirmation email.
 *
 * @Async dispatches the listener body to Spring's task executor instead
 * of running it on the caller's thread. The transaction-phase binding
 * is still synchronous - Spring waits for AFTER_COMMIT, then hands the
 * event to the executor. The listener body therefore has NO inherited
 * transaction; if it needs the database it must open its own via
 * @Transactional(propagation = REQUIRES_NEW).
 *
 * Exceptions thrown from an @Async listener are NOT propagated to the
 * publisher - see Spring's AsyncUncaughtExceptionHandler.
 *
 * @EnableAsync must be present on Application.groovy for @Async to take
 * effect; without it the annotation is silently a no-op.
 */
class NotificationListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationListener)

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    void onOrderPlaced(OrderPlacedEvent event) {
        log.info('Off-thread: sending confirmation email for order {} (total {})',
                event.orderId, event.total)
        // Real implementation would call emailService.send(...)
    }
}
