// Place your Spring DSL code here
beans = {
    // Grails only auto-registers a class under grails-app/services/ as a
    // Spring bean when its name ends in 'Service'. These listener classes end
    // in 'Listener', so Grails does NOT register them - which means Spring's
    // EventListenerMethodProcessor never scans their @EventListener /
    // @TransactionalEventListener methods and the listeners silently never
    // fire. Registering them explicitly here makes them beans so their
    // listener methods are wired at startup.
    customerLifetimeValueListener(example.CustomerLifetimeValueListener)
    auditListener(example.AuditListener)
    notificationListener(example.NotificationListener)
    workOrderAssignmentListener(example.WorkOrderAssignmentListener)

    // @TransactionalEventListener (the AFTER_COMMIT listeners) is only honored
    // when a TransactionalEventListenerFactory bean exists. Spring normally
    // contributes one through @EnableTransactionManagement, but a plain Grails
    // app does not register it, so the AFTER_COMMIT listeners would silently
    // never fire even though plain @EventListener beans do. Declaring it here
    // is what makes the transaction-bound phases work.
    transactionalEventListenerFactory(org.springframework.transaction.event.TransactionalEventListenerFactory)
}
