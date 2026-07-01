package example

/**
 * Lifecycle of a customer request. SUBMITTED is the entry state;
 * COMPLETED and CANCELLED are terminal. The only automated transition
 * this guide drives is SUBMITTED -> IN_PROGRESS, performed by the
 * synchronous orchestration listener when work is planned.
 */
enum CustomerRequestStatus {
    SUBMITTED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED

    boolean isTerminal() {
        this == COMPLETED || this == CANCELLED
    }
}
