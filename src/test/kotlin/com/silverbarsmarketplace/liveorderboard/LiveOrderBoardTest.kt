package com.silverbarsmarketplace.liveorderboard


import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal
import java.util.*

class LiveOrderBoardTest {

    @Test
    fun ableToAddBuyOrders() {

        val singleBuyOrder = Order(
            orderId = UUID.randomUUID().toString(),
            userId = "John Smith",
            orderQuantity = BigDecimal(101),
            pricePerKg = BigDecimal(501),
            orderType = OrderType.BUY
        )

        val isFirstOrderAdded = LiveOrderBoard.submitOrder(singleBuyOrder)
        assertEquals(OrderStatus.SUBMISSION_ACCEPTED, isFirstOrderAdded)

        val firstExpectedSummaryBoard = SummaryInformation(buy = sortedMapOf(singleBuyOrder.pricePerKg to singleBuyOrder.orderQuantity), sell = sortedMapOf())

        val firstSummaryInformation = LiveOrderBoard.getSummaryInformation()
        assertEquals(firstExpectedSummaryBoard, firstSummaryInformation)

        val anotherSingleBuyOrder = Order(
            orderId = UUID.randomUUID().toString(),
            userId = "Jane Doe",
            orderQuantity = BigDecimal(101),
            pricePerKg = BigDecimal(501),
            orderType = OrderType.BUY
        )

        val isSecondOrderAdded = LiveOrderBoard.submitOrder(anotherSingleBuyOrder)
        assertEquals(OrderStatus.SUBMISSION_ACCEPTED, isSecondOrderAdded)

        val secondExpectedSummaryBoard = SummaryInformation(buy = sortedMapOf(singleBuyOrder.pricePerKg to singleBuyOrder.orderQuantity.add(anotherSingleBuyOrder.orderQuantity)), sell = sortedMapOf())
        val secondSummaryInformation = LiveOrderBoard.getSummaryInformation()
        assertEquals(secondExpectedSummaryBoard, secondSummaryInformation)

        val yetAnotherSingleBuyOrder = Order(
            orderId = UUID.randomUUID().toString(),
            userId = "Muhammad Ali",
            orderQuantity = BigDecimal(101),
            pricePerKg = BigDecimal(601),
            orderType = OrderType.BUY
        )

        val isThirdOrderAdded = LiveOrderBoard.submitOrder(yetAnotherSingleBuyOrder)
        assertEquals(OrderStatus.SUBMISSION_ACCEPTED, isThirdOrderAdded)

        val thirdExpectedSummaryBoard = SummaryInformation(buy = sortedMapOf(yetAnotherSingleBuyOrder.pricePerKg to yetAnotherSingleBuyOrder.orderQuantity, singleBuyOrder.pricePerKg to singleBuyOrder.orderQuantity.add(anotherSingleBuyOrder.orderQuantity)), sell = sortedMapOf())
        val thirdSummaryInformation = LiveOrderBoard.getSummaryInformation()
        assertEquals(thirdExpectedSummaryBoard, thirdSummaryInformation)

    }

    @Test
    fun unableToAddOrderWithSameOrderNumber() {

        val singleBuyOrder = Order(
            orderId = UUID.randomUUID().toString(),
            userId = "John Smith",
            orderQuantity = BigDecimal(-101),
            pricePerKg = BigDecimal(-501),
            orderType = OrderType.SELL
        )

        val isFirstOrderAdded = LiveOrderBoard.submitOrder(singleBuyOrder)
        assertEquals(OrderStatus.SUBMISSION_REJECTED_INVALID, isFirstOrderAdded)

    }

}