package example

/**
 * Lifecycle of a work order. A work order starts OPEN with no assignee.
 * Assigning an employee moves it to PLANNED; finishing the job moves it
 * to COMPLETED. This guide drives the OPEN -> PLANNED transition.
 */
enum WorkOrderStatus {
    OPEN,
    PLANNED,
    COMPLETED
}
