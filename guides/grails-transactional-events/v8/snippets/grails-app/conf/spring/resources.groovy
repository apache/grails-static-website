beans = {
    // The listeners are ordinary services, so Grails auto-registers them - no
    // wiring needed there. This is the one bean the pattern does need: Grails
    // uses GORM's @Transactional, not Spring's @EnableTransactionManagement, so
    // it never registers a TransactionalEventListenerFactory, and without one
    // every @TransactionalEventListener silently no-ops. Declaring it here is
    // what makes the AFTER_COMMIT phases fire.
    transactionalEventListenerFactory(org.springframework.transaction.event.TransactionalEventListenerFactory)
}
