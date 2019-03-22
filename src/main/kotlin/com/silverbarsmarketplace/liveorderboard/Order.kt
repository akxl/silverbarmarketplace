package com.silverbarsmarketplace.liveorderboard

import java.math.BigDecimal

data class Order(
    val orderId: String,
    val userId: String,
    val orderQuantity: BigDecimal,
    val pricePerKg: BigDecimal,
    val orderType: OrderType
)


enum class OrderType {
    BUY,
    SELL
}