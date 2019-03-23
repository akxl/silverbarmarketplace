package com.silverbarsmarketplace.liveorderboard

enum class OrderStatus(val message: String) {
    SUBMISSION_ACCEPTED("Order submitted successfully"),
    SUBMISSION_REJECTED_INVALID("Order submission rejected - Price must be >= 0 and Quantity must be >0."),
    SUBMISSION_REJECTED_DUPLICATE("Order submission was rejected - This order is a duplication of a previously successfully submitted order."),
    CANCELLATION_ACCEPTED("Order cancelled successfully."),
    CANCELLATION_REJECTED_NOT_FOUND("Order cancellation rejected - No order found.")
}