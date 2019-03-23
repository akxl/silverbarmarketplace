package com.silverbarsmarketplace.liveorderboard

import java.math.BigDecimal
import java.util.*

class LiveOrderBoard {

    private val orders: MutableSet<Order> = mutableSetOf()
    private val buySummaryInformation: SortedMap<BigDecimal, BigDecimal> = TreeMap()
    private val sellSummaryInformation: SortedMap<BigDecimal, BigDecimal> = TreeMap()

    fun getSummaryInformation(): SummaryInformation = SummaryInformation(getBuySummaryInformation(), getSellSummaryInformation())

    private fun getBuySummaryInformation() = buySummaryInformation.toSortedMap(Collections.reverseOrder())

    private fun getSellSummaryInformation() = sellSummaryInformation.toSortedMap()

    @Synchronized
    fun submitOrder(order: Order): OrderStatus {

        val isOrderValid = checkForValidOrder(order)
        if (isOrderValid == OrderStatus.SUBMISSION_REJECTED_INVALID) {
            return isOrderValid
        }

        val isNewOrder = orders.add(order)
        if (isNewOrder == false) {
            return OrderStatus.SUBMISSION_REJECTED_DUPLICATE
        }

        return if (order.orderType == OrderType.BUY) {
            val isNewTotalQuantityComputed = buySummaryInformation.computeIfPresent(order.pricePerKg) { _, totalQuantity -> totalQuantity.add(order.orderQuantity) } != null
            val isNewPriceAdded = buySummaryInformation.putIfAbsent(order.pricePerKg, order.orderQuantity) == null
            checkIfSubmissionIsSuccessful(isNewTotalQuantityComputed, isNewPriceAdded)
        } else  {
            val isNewTotalQuantityComputed = sellSummaryInformation.computeIfPresent(order.pricePerKg) { _, totalQuantity -> totalQuantity.add(order.orderQuantity) } != null
            val isNewPriceAdded = sellSummaryInformation.putIfAbsent(order.pricePerKg, order.orderQuantity) == null
            checkIfSubmissionIsSuccessful(isNewTotalQuantityComputed, isNewPriceAdded)
        }
    }

    private fun checkForValidOrder(order: Order): OrderStatus? {
        return if (order.orderQuantity > BigDecimal.ZERO && order.pricePerKg >= BigDecimal.ZERO) null else OrderStatus.SUBMISSION_REJECTED_INVALID
    }

    private fun checkIfSubmissionIsSuccessful(isNewTotalQuantityComputed: Boolean, isNewPriceAdded: Boolean): OrderStatus {
        if ((isNewTotalQuantityComputed || isNewPriceAdded) == false) {
            throw Exception("Order submission failed. Internal error. Please contact the developer.")
        }
        return OrderStatus.SUBMISSION_ACCEPTED
    }

    @Synchronized
    fun cancelOrder(order: Order): OrderStatus {

        val orderFoundAndRemoved = orders.remove(order)
        if (orderFoundAndRemoved == false) {
            return OrderStatus.CANCELLATION_REJECTED_NOT_FOUND
        }

        return if (order.orderType == OrderType.BUY) {
            val isCancellationSuccessful = if (buySummaryInformation[order.pricePerKg] != null && buySummaryInformation[order.pricePerKg] == order.orderQuantity) {
                buySummaryInformation.remove(order.pricePerKg)
            } else {
                buySummaryInformation.computeIfPresent(order.pricePerKg) { _, totalQuantity -> totalQuantity.subtract(order.orderQuantity) }
            }
            checkIfCancellationIsSuccessful(isCancellationSuccessful)
        } else {
            val isCancellationSuccessful = if (sellSummaryInformation[order.pricePerKg] != null && sellSummaryInformation[order.pricePerKg] == order.orderQuantity) {
                sellSummaryInformation.remove(order.pricePerKg)
            } else {
                sellSummaryInformation.computeIfPresent(order.pricePerKg) { _, totalQuantity -> totalQuantity.subtract(order.orderQuantity) }
            }
            checkIfCancellationIsSuccessful(isCancellationSuccessful)
        }
    }

    private fun checkIfCancellationIsSuccessful(cancellationIsSuccessful: BigDecimal?): OrderStatus {
        if (cancellationIsSuccessful == null) {
            throw Exception("Order submission failed. Internal error. Please contact the developer.")
        }
        return OrderStatus.CANCELLATION_ACCEPTED
    }

}

data class SummaryInformation(val buy: SortedMap<BigDecimal, BigDecimal>, val sell: SortedMap<BigDecimal, BigDecimal>)