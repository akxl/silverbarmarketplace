package com.silverbarsmarketplace.liveorderboard

import java.math.BigDecimal
import java.util.*

object LiveOrderBoard {

    private val orders: MutableSet<Order> = mutableSetOf()
    private val buySummaryInformation: SortedMap<BigDecimal, BigDecimal> = TreeMap()
    private val sellSummaryInformation: SortedMap<BigDecimal, BigDecimal> = TreeMap()

    fun getOrders(): Set<Order> = orders.toSet()

    fun getSummaryInformation(): SummaryInformation {
        return SummaryInformation(
            buy = getBuySummaryInformation(),
            sell = getSellSummaryInformation()
        )
    }

    private fun getBuySummaryInformation() = buySummaryInformation.toSortedMap(Collections.reverseOrder())

    private fun getSellSummaryInformation() = sellSummaryInformation.toSortedMap()

    // is synchronized required?
    fun submitOrder(order: Order): Boolean {
        val isNewOrder = orders.add(order)
        return if (isNewOrder && order.orderType == OrderType.BUY) {
            val isNewTotalQuantityComputed = buySummaryInformation.computeIfPresent(order.pricePerKg) { _, totalQuantity -> totalQuantity.add(order.orderQuantity) } != null
            val isNewPriceAdded = buySummaryInformation.putIfAbsent(order.pricePerKg, order.orderQuantity) == null
            (isNewTotalQuantityComputed || isNewPriceAdded)
        } else if (isNewOrder && order.orderType == OrderType.SELL) {
            val isNewTotalQuantityComputed = sellSummaryInformation.computeIfPresent(order.pricePerKg) { _, totalQuantity -> totalQuantity.add(order.orderQuantity) } != null
            val isNewPriceAdded = sellSummaryInformation.putIfAbsent(order.pricePerKg, order.orderQuantity) == null
            (isNewTotalQuantityComputed || isNewPriceAdded)
        } else {
            false
        }
    }

}

data class SummaryInformation(val buy: SortedMap<BigDecimal, BigDecimal>, val sell: SortedMap<BigDecimal, BigDecimal>)